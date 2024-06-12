package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.ProjectRepositoryFunction;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.function.EffectiveVersion;
import org.dmfs.gver.dsl.procedure.CreateTag;
import org.dmfs.jems2.Optional;
import org.dmfs.semver.StrictParser;
import org.dmfs.semver.Version;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

import static org.dmfs.gver.dsl.ReleaseType.PRERELEASE;


/**
 * A Gradle {@link Task} that tags the current HEAD with a new pre-release version.
 * <p>
 * This task will throw an {@link IllegalStateException} when the current HEAD is not on a pre-release branch.
 */
public class TagPreReleaseTask extends DefaultTask
{
    public TagPreReleaseTask()
    {
        setGroup("gver");
        setDescription("Tags the HEAD commit with a new pre-release version.");
    }

    @TaskAction
    public void perform() throws IOException
    {
        try (Repository repository = ProjectRepositoryFunction.INSTANCE.value(getProject()))
        {
            Optional<Version> version =
                new EffectiveVersion(PRERELEASE::equals,
                    (GitVersionConfig) getProject().getExtensions().getByName("gver"),
                    new StrictParser().parse(getProject().getVersion().toString())).value(repository);

            if (!version.isPresent())
            {
                throw new IllegalStateException("Current head is not allowed to be tagged with a pre-release version.");
            }
            new CreateTag(getLogger()::lifecycle, version.value())
                .process(repository);
        }
    }
}