package org.dmfs.gradle.gitversion.git;

import org.dmfs.gradle.gitversion.git.changetypefacories.Condition;
import org.dmfs.jems2.Function;
import org.dmfs.semver.*;


public enum ChangeType implements Function<Version, Version>
{
    UNKNOWN(NextPreRelease::new),
    PATCH(PatchPreRelease::new),
    MINOR(MinorPreRelease::new),
    MAJOR(MajorPreRelease::new);

    private final Function<? super Version, ? extends Version> mVersionFactory;


    ChangeType(Function<? super Version, ? extends Version> versionFactory)
    {
        mVersionFactory = versionFactory;
    }


    public ChangeTypeStrategy when(Condition condition)
    {
        return (commit, branches) -> condition.matches(commit, branches) ? ChangeType.this : UNKNOWN;
    }


    @Override
    public Version value(Version old)
    {
        return mVersionFactory.value(old);
    }

}
