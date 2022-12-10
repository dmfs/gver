package org.dmfs.gitversion.git;

import org.dmfs.jems2.Optional;
import org.dmfs.semver.Version;

import static org.dmfs.jems2.optional.Absent.absent;


public final class WithoutBuildMeta implements Version
{
    private final Version mDelegate;


    public WithoutBuildMeta(Version delegate)
    {
        mDelegate = delegate;
    }


    @Override
    public int major()
    {
        return mDelegate.major();
    }


    @Override
    public int minor()
    {
        return mDelegate.minor();
    }


    @Override
    public int patch()
    {
        return mDelegate.patch();
    }


    @Override
    public Optional<String> preRelease()
    {
        return mDelegate.preRelease();
    }


    @Override
    public Optional<String> build()
    {
        return absent();
    }
}
