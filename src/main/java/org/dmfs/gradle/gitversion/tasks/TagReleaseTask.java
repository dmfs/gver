package org.dmfs.gradle.gitversion.tasks;

import org.dmfs.gradle.gitversion.dsl.GitVersionConfig;
import org.dmfs.gradle.gitversion.utils.ProjectRepositoryFunction;
import org.dmfs.jems2.optional.First;
import org.dmfs.semver.Release;
import org.dmfs.semver.StrictParser;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;


/**
 * A Gradle {@link Task} that tags the current HEAD with a new release version.
 * Note, that this task will throw an {@link IllegalStateException} when the current HEAD is not on a release branch.
 */
public class TagReleaseTask extends DefaultTask
{
    @TaskAction
    public void perform() throws IOException, GitAPIException
    {
        try (Repository r = ProjectRepositoryFunction.INSTANCE.value(getProject()))
        {
            Git git = new Git(r);
            GitVersionConfig config = (GitVersionConfig) getProject().getExtensions().getByName("gitVersion");
            ObjectId head = r.resolve("HEAD");

            if (!new First<>(branch -> config.mReleaseBranchPattern.matcher(branch).lookingAt(), git.nameRev()
                .addPrefix("refs/heads")
                .add(head).call().values()).isPresent())
            {
                throw new IllegalStateException("Not adding a release tag on non-release branch");
            }

            git.tag()
                .setObjectId(r.parseCommit(head))
                .setName(new VersionSequence(new Release(new StrictParser().parse(getProject().getVersion().toString()))).toString())
                .call();
        }
    }
}
