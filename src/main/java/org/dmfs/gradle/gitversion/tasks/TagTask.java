package org.dmfs.gradle.gitversion.tasks;

import org.dmfs.gradle.gitversion.utils.ProjectRepositoryFunction;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;


/**
 * A {@link Task} that adds a tag with the current version to the current HEAD.
 */
public class TagTask extends DefaultTask
{
    @TaskAction
    public void perform() throws IOException, GitAPIException
    {
        try (Repository r = ProjectRepositoryFunction.INSTANCE.value(getProject()))
        {
            new Git(r).tag().setName(getProject().getVersion().toString()).call();
        }
    }
}
