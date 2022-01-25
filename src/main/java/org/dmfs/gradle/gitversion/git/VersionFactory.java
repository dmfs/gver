package org.dmfs.gradle.gitversion.git;

import org.dmfs.jems2.Optional;
import org.dmfs.semver.Version;


public interface VersionFactory
{
    Version next(Version old, Optional<String> preRelease, String buildMeta);
}
