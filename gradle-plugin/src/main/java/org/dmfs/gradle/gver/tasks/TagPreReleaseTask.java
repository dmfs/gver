package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.ProjectRepositoryFunction;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.procedure.CreateTag;
import org.dmfs.semver.StrictParser;
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
            new CreateTag(
                getLogger()::lifecycle,
                PRERELEASE::equals,
                (GitVersionConfig) getProject().getExtensions().getByName("gver"),
                new StrictParser().parse(getProject().getVersion().toString()))
                .process(repository);
        }
    }
}
