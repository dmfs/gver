package org.dmfs.gradle.gitversion.git;

import org.dmfs.gradle.gitversion.git.changetypefacories.Condition;
import org.dmfs.jems2.BiFunction;
import org.dmfs.semver.*;


public enum ChangeType implements BiFunction<Version, String, Version>
{
    UNKNOWN(NextPreRelease::new),
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
        return (commit, branches) -> condition.matches(commit, branches) ? ChangeType.this : UNKNOWN;
    }


    @Override
    public Version value(Version old, String preRelease)
    {
        return mVersionFactory.value(old, preRelease);
    }
}
