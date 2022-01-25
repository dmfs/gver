package org.dmfs.gradle.gitversion;

import org.dmfs.gradle.gitversion.dsl.GitVersionConfig;
import org.dmfs.gradle.gitversion.git.GitVersion;
import org.dmfs.gradle.gitversion.git.changetypefacories.FirstOf;
import org.dmfs.gradle.gitversion.tasks.TagReleaseTask;
import org.dmfs.gradle.gitversion.tasks.TagTask;
import org.dmfs.gradle.gitversion.utils.ProjectRepositoryFunction;
import org.dmfs.jems2.Single;
import org.dmfs.jems2.single.Frozen;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


/**
 * A plugin to provide automatic semantic versioning to Gradle projects.
 */
@NonNullApi
public final class GitVersionPlugin implements Plugin<Project>
{

    @Override
    public void apply(Project project)
    {
        GitVersionConfig extension = project.getExtensions().create("gitVersion", GitVersionConfig.class);

        project.setVersion(
            new Object()
            {
                private final Single<String> mVersion = new Frozen<>(
                    () -> {
                        try (Repository r = ProjectRepositoryFunction.INSTANCE.value(project))
                        {
                            return new VersionSequence(
                                new GitVersion(
                                    new FirstOf(extension.mChangeTypeStrategy.mChangeTypeStrategies))
                                    .value(r)).toString();

                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
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

        project.subprojects(subproject -> subproject.setVersion(project.getVersion()));
    }
}
