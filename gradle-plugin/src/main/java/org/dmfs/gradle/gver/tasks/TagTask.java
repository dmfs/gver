package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.ProjectRepositoryFunction;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.procedure.CreateTag;
import org.dmfs.jems2.predicate.Anything;
import org.dmfs.semver.StrictParser;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;


/**
 * A {@link Task} that adds a tag with the current version to the current HEAD.
 * <p>
 * The decision whether the tag is a release or pre-release version depends on the {@code tag} configuration.
 */
public class TagTask extends DefaultTask
{
    public TagTask()
    {
        setGroup("gver");
        setDescription("Tags the HEAD commit with the current project version.");
    }

    @TaskAction
    public void perform() throws IOException
    {
        try (Repository repository = ProjectRepositoryFunction.INSTANCE.value(getProject()))
        {
            new CreateTag(
                getLogger()::lifecycle,
                new Anything<>(),
                (GitVersionConfig) getProject().getExtensions().getByName("gver"),
                new StrictParser().parse(getProject().getVersion().toString()))
                .process(repository);
        }
    }
}
