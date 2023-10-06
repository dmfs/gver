package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.ProjectRepositoryFunction;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.jems2.iterable.Mapped;
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

import static org.eclipse.jgit.lib.Constants.R_TAGS;


/**
 * A Gradle {@link Task} that tags the current HEAD with a new release version.
 * Note, that this task will throw an {@link IllegalStateException} when the current HEAD is not on a release branch.
 */
public class TagReleaseTask extends DefaultTask
{
    public TagReleaseTask()
    {
        setGroup("gver");
        setDescription("Tags the HEAD commit with a new release version.");
    }

    @TaskAction
    public void perform() throws IOException, GitAPIException
    {
        try (Repository r = ProjectRepositoryFunction.INSTANCE.value(getProject()))
        {
            Git git = new Git(r);
            GitVersionConfig config = (GitVersionConfig) getProject().getExtensions().getByName("gver");
            ObjectId head = r.resolve("HEAD");

            if (!new First<>(branch -> config.mReleaseBranchPattern.matcher(branch).lookingAt(), git.nameRev()
                .addPrefix("refs/heads")
                .add(head).call().values()).isPresent())
            {
                throw new IllegalStateException("Not adding a release tag on non-release branch");
            }

            String version = new VersionSequence(new Release(new StrictParser().parse(getProject().getVersion().toString()))).toString();

            if (!new First<>(version::equals,
                new Mapped<>(tag -> tag.getName().startsWith(R_TAGS) ? tag.getName().substring(R_TAGS.length()) : tag.getName(),
                    git.tagList().call())).isPresent())
            {
                git.tag()
                    .setObjectId(r.parseCommit(head))
                    .setName(version)
                    .call();
            }
            else
            {
                getLogger().lifecycle("Tag {} already exists. Not adding tag.", version);
            }
        }
    }
}
