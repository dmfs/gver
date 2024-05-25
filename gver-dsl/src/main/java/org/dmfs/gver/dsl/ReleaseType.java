package org.dmfs.gver.dsl;

import org.dmfs.jems2.Function;
import org.dmfs.semver.Release;
import org.dmfs.semver.Version;

public enum ReleaseType implements Function<Version, Version>
{
    RELEASE(Release::new),
    PRERELEASE(v -> v),
    NONE(version -> {throw new IllegalArgumentException("current head is not eligible for any release type");});

    private final Function<Version, Version> versionFunction;

    ReleaseType(Function<Version, Version> versionFunction) {this.versionFunction = versionFunction;}

    @Override
    public Version value(Version version)
    {
        return versionFunction.value(version);
    }
}
