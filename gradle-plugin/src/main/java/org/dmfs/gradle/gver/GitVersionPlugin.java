package org.dmfs.gradle.gver;

import org.dmfs.gradle.gver.tasks.TagReleaseTask;
import org.dmfs.gradle.gver.tasks.TagTask;
import org.dmfs.gradle.gver.utils.ProjectRepositoryFunction;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.git.GitVersion;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.gradle.gver.tasks.VersionTask;
import org.dmfs.jems2.Single;
import org.dmfs.jems2.single.Frozen;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Optional;


/**
 * A plugin to provide automatic semantic versioning to Gradle projects.
 */
@NonNullApi
public final class GitVersionPlugin implements Plugin<Project>
{

    @Override
    public void apply(Project project)
    {
        GitVersionConfig extension = project.getExtensions().create("gver", GitVersionConfig.class);

        project.setVersion(
            new Object()
            {
                private final Single<String> mVersion = new Frozen<>(
                    () -> {
                        long start = System.currentTimeMillis();
                        try (Repository repo = ProjectRepositoryFunction.INSTANCE.value(project))
                        {
                            return new VersionSequence(
                                new GitVersion(
                                    new FirstOf(extension.mChangeTypeStrategy.mChangeTypeStrategies),
                                    extension.mSuffixes,
                                    branch -> extension.mPreReleaseStrategies.mBranchConfigs.stream()
                                        .map(preReleaseStrategy -> preReleaseStrategy.apply(branch))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .findFirst()
                                        .orElse("alpha"))
                                    .value(repo)).toString();

                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                        finally
                        {
                            project.getLogger().debug("gver execution time: {} ms", System.currentTimeMillis() - start);
                        }
                    }
                );


                @Override
                public String toString()
                {
                    return mVersion.value();
                }
            }
        );

        project.getTasks().register("gitTag", TagTask.class);
        project.getTasks().register("gitTagRelease", TagReleaseTask.class);
        project.getTasks().register("gitVersion", VersionTask.class);

        project.subprojects(subproject -> subproject.setVersion(project.getVersion()));
    }
}
