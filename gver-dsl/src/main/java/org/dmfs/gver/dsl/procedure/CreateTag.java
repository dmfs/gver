package org.dmfs.gver.dsl.procedure;

import org.dmfs.gver.dsl.ApplicableReleaseTypes;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.Log;
import org.dmfs.gver.dsl.ReleaseType;
import org.dmfs.jems2.FragileProcedure;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.Predicate;
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
    private final Predicate<ReleaseType> mReleaseType;
    private final GitVersionConfig mGitVersionConfig;
    private final Version mVersion;

    public CreateTag(Log logger, Predicate<ReleaseType> releaseType, GitVersionConfig gitVersionConfig, Version version)
    {
        mLog = logger;
        mReleaseType = releaseType;
        mGitVersionConfig = gitVersionConfig;
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

            Optional<String> versionString =
                new org.dmfs.jems2.optional.Mapped<>(
                    Object::toString,
                    new org.dmfs.jems2.optional.Mapped<>(
                        VersionSequence::new,
                        new org.dmfs.jems2.optional.Mapped<>(
                            releaseType -> releaseType.value(mVersion),
                            new First<>(
                                mReleaseType,
                                new ApplicableReleaseTypes(mGitVersionConfig, repository, head, repository.getBranch())))));

            if (!versionString.isPresent())
            {
                throw new IllegalStateException("No release type found that's applicable to the current head");
            }

            if (!new First<>(versionString.value()::equals,
                new Mapped<>(tag -> tag.getName().startsWith(R_TAGS) ? tag.getName().substring(R_TAGS.length()) : tag.getName(),
                    git.tagList().call())).isPresent())
            {
                git.tag()
                    .setObjectId(head)
                    .setName(versionString.value())
                    .call();
            }
            else
            {
                mLog.log("Tag {} already exists. Not adding tag.", versionString.value());
            }
        }
        catch (GitAPIException apiException)
        {
            throw new IOException("Error while executing Git Command", apiException);
        }
    }
}
