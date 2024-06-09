package org.dmfs.gver.dsl.function;

import org.dmfs.gver.dsl.ApplicableReleaseTypes;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.ReleaseType;
import org.dmfs.jems2.FragileFunction;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.Predicate;
import org.dmfs.jems2.optional.First;
import org.dmfs.semver.Version;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;

public final class EffectiveVersion implements FragileFunction<Repository, Optional<Version>, IOException>
{
    private final Predicate<ReleaseType> mReleaseType;
    private final GitVersionConfig mGitVersionConfig;
    private final Version mVersion;

    public EffectiveVersion(Predicate<ReleaseType> releaseType, GitVersionConfig gitVersionConfig, Version version)
    {
        mReleaseType = releaseType;
        mGitVersionConfig = gitVersionConfig;
        mVersion = version;
    }

    @Override
    public Optional<Version> value(Repository repository) throws IOException
    {
        ObjectId headId = repository.resolve("HEAD");
        RevCommit head = repository.parseCommit(headId);
        return new org.dmfs.jems2.optional.Mapped<>(
            releaseType -> releaseType.value(mVersion),
            new First<>(
                mReleaseType,
                new ApplicableReleaseTypes(mGitVersionConfig, repository, head, repository.getBranch())));

    }
}
