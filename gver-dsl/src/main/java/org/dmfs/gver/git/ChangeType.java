package org.dmfs.gver.git;

import org.dmfs.gver.git.changetypefacories.Condition;
import org.dmfs.jems2.BiFunction;
import org.dmfs.semver.*;


public enum ChangeType implements BiFunction<Version, String, Version>
{
    INVALID((version, preRelease) -> {
        throw new IllegalArgumentException("Invalid change after version " + new VersionSequence(version));
    }),
    UNKNOWN(NextPreRelease::new),
    NONE((version, preRelease) -> version), // same version
    PATCH(PatchPreRelease::new),
    MINOR(MinorPreRelease::new),
    MAJOR(MajorPreRelease::new);

    private final BiFunction<? super Version, String, ? extends Version> mVersionFactory;


    ChangeType(BiFunction<? super Version, String, ? extends Version> versionFactory)
    {
        mVersionFactory = versionFactory;
    }


    public ChangeTypeStrategy when(Condition condition)
    {
        return (repository, commit, branches) -> condition.matches(repository, commit, branches) ? ChangeType.this : UNKNOWN;
    }


    @Override
    public Version value(Version old, String preRelease)
    {
        return mVersionFactory.value(old, preRelease);
    }
}
