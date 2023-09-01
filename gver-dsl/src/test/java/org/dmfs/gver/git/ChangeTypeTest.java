package org.dmfs.gver.git;

import org.dmfs.semver.PreRelease;
import org.dmfs.semver.Release;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.semver.confidence.SemVer.preRelease;
import static org.dmfs.semver.confidence.SemVer.release;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.quality.Core.*;

@Confidence
class ChangeTypeTest
{

    Assertion invalid_type_throws = assertionThat(INVALID,
        has("version",
            invalid -> () -> invalid.value(new Release(1, 2, 3), "alpha"),
            is(throwing(IllegalArgumentException.class))));


    Assertion major_increases_major = assertionThat(MAJOR,
        allOf(
            has("version",
                major -> major.value(new Release(1, 2, 3), "alpha"),
                is(preRelease(2, 0, 0, "alpha")))));

    Assertion major_increases_prerelease = assertionThat(MAJOR,
        allOf(
            has("version",
                major -> major.value(new PreRelease(new Release(2, 0, 0), "alpha.1"), "alpha"),
                is(preRelease(2, 0, 0, "alpha.2")))));


    Assertion minor_increases_minor = assertionThat(MINOR,
        allOf(
            has("version",
                major -> major.value(new Release(1, 2, 3), "alpha"),
                is(preRelease(1, 3, 0, "alpha")))));

    Assertion minor_increases_prerelease = assertionThat(MINOR,
        allOf(
            has("version",
                major -> major.value(new PreRelease(new Release(1, 2, 0), "alpha.1"), "alpha"),
                is(preRelease(1, 2, 0, "alpha.2")))));


    Assertion patch_increases_patch = assertionThat(PATCH,
        allOf(
            has("version",
                major -> major.value(new Release(1, 2, 3), "alpha"),
                is(preRelease(1, 2, 4, "alpha")))));

    Assertion patch_increases_prerelease = assertionThat(PATCH,
        allOf(
            has("version",
                major -> major.value(new PreRelease(new Release(1, 2, 3), "alpha.1"), "alpha"),
                is(preRelease(1, 2, 3, "alpha.2")))));


    Assertion none_keeps_same_release = assertionThat(NONE,
        allOf(
            has("version",
                major -> major.value(new Release(1, 2, 3), "alpha"),
                is(release(1, 2, 3)))));

    Assertion none_keeps_same_prereelase_version = assertionThat(NONE,
        allOf(
            has("version",
                major -> major.value(new PreRelease(new Release(1, 2, 3), "alpha.1"), "alpha"),
                is(preRelease(1, 2, 3, "alpha.1")))));


    Assertion unknown_returns_next_minor_prerelease = assertionThat(UNKNOWN,
        allOf(
            has("version",
                major -> major.value(new Release(1, 2, 3), "alpha"),
                is(preRelease(1, 3, 0, "alpha")))));

    Assertion unknown_returns_next_prerelease = assertionThat(UNKNOWN,
        allOf(
            has("version",
                major -> major.value(new PreRelease(new Release(1, 2, 3), "alpha.1"), "alpha"),
                is(preRelease(1, 2, 3, "alpha.2")))));
}