package org.dmfs.gradle.gitversion.git;

import org.dmfs.gradle.gitversion.git.changetypefacories.Condition;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.single.Backed;
import org.dmfs.semver.*;


public enum ChangeType implements VersionFactory
{
    UNKNOWN((version, p, s) -> new NextPreRelease(version, new Backed<>(p, "alpha").value(), s)),
    PRERELEASE((version, p, s) -> new NextPreRelease(version, new Backed<>(p, "alpha").value(), s)),
    PATCH((version, p, s) -> new PatchPreRelease(version, new Backed<>(p, "alpha").value(), s)),
    MINOR((version, p, s) -> new MinorPreRelease(version, new Backed<>(p, "alpha").value(), s)),
    MAJOR((version, p, s) -> new MajorPreRelease(version, new Backed<>(p, "alpha").value(), s));

    private final VersionFactory mVersionFactory;


    ChangeType(VersionFactory versionFactory)
    {
        mVersionFactory = versionFactory;
    }


    public ChangeTypeStrategy when(Condition condition)
    {
        return (commit, branches) -> condition.matches(commit, branches) ? ChangeType.this : UNKNOWN;
    }


    @Override
    public Version next(Version old, Optional<String> preRelease, String buildMeta)
    {
        return mVersionFactory.next(old, preRelease, buildMeta);
    }

}
