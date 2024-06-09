package org.dmfs.gver.dsl.procedure;

import org.dmfs.gver.dsl.Log;
import org.dmfs.jems2.FragileProcedure;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.First;
import org.dmfs.semver.Version;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

public final class CreateTag implements FragileProcedure<Repository, IOException>
{
    private final Log mLog;
    private final Version mVersion;

    public CreateTag(Log logger, Version version)
    {
        mLog = logger;
        mVersion = version;
    }

    @Override
    public void process(Repository repository) throws IOException
    {
        try
        {
            Git git = new Git(repository);

            if (git.status().call().hasUncommittedChanges())
            {
                throw new IOException(
                    "Not creating tag on dirty working tree. Please commit, stash or revert any uncommitted changes first."
                );
            }

            ObjectId headId = repository.resolve("HEAD");
            RevCommit head = repository.parseCommit(headId);

            String versionString = new VersionSequence(mVersion).toString();

            if (!new First<>(versionString::equals,
                new Mapped<>(tag -> tag.getName().startsWith(R_TAGS) ? tag.getName().substring(R_TAGS.length()) : tag.getName(),
                    git.tagList().call())).isPresent())
            {
                git.tag()
                    .setObjectId(head)
                    .setName(versionString)
                    .call();
            }
            else
            {
                mLog.log("Tag {} already exists. Not adding tag.", versionString);
            }
        }
        catch (GitAPIException apiException)
        {
            throw new IOException("Error while executing Git Command", apiException);
        }
    }
}
