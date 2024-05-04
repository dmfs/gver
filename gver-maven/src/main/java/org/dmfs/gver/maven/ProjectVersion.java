package org.dmfs.gver.maven;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.git.GitVersion;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.jems2.FragileFunction;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.optional.Mapped;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.single.Backed;
import org.dmfs.semver.Version;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * The version of the current Git hosted Maven project.
 */
@Named
@Singleton
public final class ProjectVersion implements FragileFunction<Function<Version, Version>, Version, MojoExecutionException>
{
    @Inject
    private MavenProject mProject;

    private Version mVersion;

    @Override
    public Version value(Function<Version, Version> decorator) throws MojoExecutionException
    {
        // TODO get rid of null
        if (mVersion != null)
        {
            return mVersion;
        }

        Object pluginConfig = mProject.getBuildPlugins()
            .stream()
            .filter(plugin -> "gver-maven".equals(plugin.getArtifactId()))
            .findFirst()
            .map(Plugin::getConfiguration)
            .map(c -> extractNestedString("config", (Xpp3Dom) c))
            .get();

        try
        {
            Repository repo = new FileRepositoryBuilder().findGitDir(mProject.getBasedir()).setMustExist(true).build();

            CompilerConfiguration cc = new CompilerConfiguration();
            cc.setScriptBaseClass(DelegatingScript.class.getName());
            GroovyShell shell = new GroovyShell(DelegatingScript.class.getClassLoader(), new Binding(), cc);
            DelegatingScript configClosure = (DelegatingScript) shell.parse(pluginConfig.toString());

            GitVersionConfig config = new GitVersionConfig();
            configClosure.setDelegate(config);
            configClosure.run();

            mVersion = decorator.value(new GitVersion(
                new FirstOf(config.mChangeTypeStrategy.mChangeTypeStrategies),
                config.mSuffixes,
                branch -> config.mPreReleaseStrategies.mBranchConfigs.stream()
                    .map(it -> it.apply(branch))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse("alpha"))
                .value(repo));

            return mVersion;
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e);
        }
    }


    /**
     * Extracts nested values from the given config object into a List.
     *
     * @param childname the name of the first subelement that contains the list
     * @param config    the actual config object
     */
    private String extractNestedString(String childname, Xpp3Dom config)
    {
        return new Backed<>(
            new Mapped<>(Xpp3Dom::getValue,
                new NullSafe<>(config.getChild(childname))), "").value();
    }

}
