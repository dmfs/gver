package org.dmfs.gver.maven;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.function.EffectiveVersion;
import org.dmfs.gver.dsl.procedure.CreateTag;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.predicate.Anything;
import org.dmfs.jems2.single.Backed;
import org.dmfs.semver.Version;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.inject.Inject;
import java.io.IOException;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Updates maven project and tag the head with the configured version.
 */
@Mojo(name = "tag-and-set-version")
@Execute(goal = "tag-and-set-version")
public final class GVerTagAndSetVersionMojo extends AbstractMojo
{
    @Inject
    private ProjectVersion projectVersion;

    @Parameter(name = "config", readonly = true, required = true)
    private Object config;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException
    {
        Optional<Version> version;
        try (Repository repo = new FileRepositoryBuilder().findGitDir(mavenProject.getBasedir()).setMustExist(true).build())
        {
            CompilerConfiguration cc = new CompilerConfiguration();
            cc.setScriptBaseClass(DelegatingScript.class.getName());
            GroovyShell shell = new GroovyShell(DelegatingScript.class.getClassLoader(), new Binding(), cc);
            DelegatingScript configClosure = (DelegatingScript) shell.parse(config.toString());

            GitVersionConfig config = new GitVersionConfig();
            configClosure.setDelegate(config);
            configClosure.run();

            version = new EffectiveVersion(new Anything<>(), config, projectVersion.value(x -> x)).value(repo);

            if (version.isPresent())
            {
                new CreateTag((message, params) -> {}, version.value()).process(repo);
            }
        }
        catch (IOException ioException)
        {
            throw new MojoExecutionException(ioException);
        }


        executeMojo(
            plugin(
                groupId("org.codehaus.mojo"),
                artifactId("versions-maven-plugin"),
                version("2.16.2")
            ),
            goal("set"),
            configuration(
                element(name("newVersion"), new VersionSequence(new Backed<>(version, projectVersion.value(x -> x)).value()).toString()),
                element(name("generateBackupPoms"), "false")
            ),
            executionEnvironment(
                mavenProject,
                mavenSession,
                pluginManager
            )
        );

    }
}
