package org.dmfs.gver.dsl.function;

import org.dmfs.gver.dsl.Conditions;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.ReleaseType;
import org.dmfs.gver.dsl.TagConfig;
import org.dmfs.gver.dsl.utils.LambdaClosure;
import org.dmfs.gver.dsl.utils.Repo;
import org.dmfs.jems2.Procedure;
import org.dmfs.jems2.predicate.Anything;
import org.dmfs.jems2.predicate.Nothing;
import org.dmfs.semver.PreRelease;
import org.dmfs.semver.Release;
import org.eclipse.jgit.lib.Repository;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.resource.Initialized;
import org.saynotobugs.confidence.junit5.engine.resource.LazyResource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.gver.dsl.ReleaseType.PRERELEASE;
import static org.dmfs.jems2.confidence.Jems2.*;
import static org.dmfs.semver.confidence.SemVer.preRelease;
import static org.dmfs.semver.confidence.SemVer.release;
import static org.saynotobugs.confidence.core.quality.Grammar.to;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResources;


@Confidence
class EffectiveVersionTest
{
    Resource<File> projectDir = new TempDir();
    Resource<Repository> testRepository = new Repo("0.0.2-alpha.1.bundle", "main", projectDir);
    Resource<GitVersionConfig> gitVersionConfig = new LazyResource<>(GitVersionConfig::new, c -> {});

    Assertion configMapsToReleaseVersion = withResources(testRepository, new Initialized<>(
            c ->
                c.tag(new LambdaClosure<Void, TagConfig>(
                    this,
                    tagConfig -> {
                        tagConfig
                            .with(ReleaseType.RELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("feat"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(ReleaseType.NONE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
                    })), gitVersionConfig),

        (repo, config) ->
            assertionThat(new EffectiveVersion(new Anything<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                maps(
                    repo,
                    to(present(release(1, 2, 3))))));

    Assertion configMapsToPreReleaseVersion = withResources(testRepository, new Initialized<>(
            c ->
                c.tag(new LambdaClosure<Void, TagConfig>(
                    this,
                    tagConfig -> {
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("feat"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(ReleaseType.NONE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
                    })), gitVersionConfig),

        (repo, config) ->
            assertionThat(new EffectiveVersion(new Anything<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                maps(
                    repo,
                    to(present(preRelease(1, 2, 3, "foobar"))))));

    Assertion filterMatchesPreReleaseVersion = withResources(testRepository, new Initialized<>(
            c ->
                c.tag(new LambdaClosure<Void, TagConfig>(
                    this,
                    tagConfig -> {
                        tagConfig
                            .with(ReleaseType.RELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("feat"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(ReleaseType.NONE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
                    })), gitVersionConfig),

        (repo, config) ->
            assertionThat(new EffectiveVersion(PRERELEASE::equals, config, new PreRelease(new Release(1, 2, 3), "foobar")),
                maps(
                    repo,
                    to(present(preRelease(1, 2, 3, "foobar"))))));

    Assertion configDoesNotMatch = withResources(testRepository, new Initialized<>(
            c ->
                c.tag(new LambdaClosure<Void, TagConfig>(
                    this,
                    tagConfig -> {
                        tagConfig
                            .with(ReleaseType.NONE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
                    })), gitVersionConfig),

        (repo, config) ->
            assertionThat(new EffectiveVersion(PRERELEASE::equals, config, new PreRelease(new Release(1, 2, 3), "foobar")),
                maps(
                    repo,
                    to(absent()))));

    Assertion filterAllVersions = withResources(testRepository, new Initialized<>(
            c ->
                c.tag(new LambdaClosure<Void, TagConfig>(
                    this,
                    tagConfig -> {
                        tagConfig
                            .with(ReleaseType.RELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("feat"::equals)));
                        tagConfig
                            .with(PRERELEASE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                        tagConfig
                            .with(ReleaseType.NONE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
                    })), gitVersionConfig),

        (repo, config) ->
            assertionThat(new EffectiveVersion(new Nothing<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                maps(
                    repo,
                    to(absent()))));
}