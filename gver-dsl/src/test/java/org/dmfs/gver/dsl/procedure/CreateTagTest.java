package org.dmfs.gver.dsl.procedure;

import org.dmfs.gver.dsl.Conditions;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.ReleaseType;
import org.dmfs.gver.dsl.TagConfig;
import org.dmfs.gver.dsl.utils.LambdaClosure;
import org.dmfs.gver.dsl.utils.Repo;
import org.dmfs.jems2.Procedure;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.predicate.Anything;
import org.dmfs.jems2.predicate.Nothing;
import org.dmfs.semver.PreRelease;
import org.dmfs.semver.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.resource.Initialized;
import org.saynotobugs.confidence.junit5.engine.resource.LazyResource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.gver.dsl.ReleaseType.PRERELEASE;
import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.saynotobugs.confidence.core.quality.Composite.has;
import static org.saynotobugs.confidence.core.quality.Grammar.soIt;
import static org.saynotobugs.confidence.core.quality.Grammar.when;
import static org.saynotobugs.confidence.core.quality.Iterable.iterates;
import static org.saynotobugs.confidence.core.quality.Object.throwing;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResources;

@Confidence
class CreateTagTest
{
    Resource<File> projectDir = new TempDir();
    Resource<Repository> testRepository = new Repo("0.0.2-alpha.1.bundle", "main", projectDir);
    Resource<GitVersionConfig> gitVersionConfig = new LazyResource<>(GitVersionConfig::new, c -> {});

    Assertion createsReleaseTag = withResources(testRepository, new Initialized<>(
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
            assertionThat(new CreateTag((message, params) -> {},
                    new Anything<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                procedureThatAffects(
                    new Text("repository"),
                    () -> repo,
                    soIt(has("tag list that",
                        repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                        iterates("refs/tags/0.0.1", "refs/tags/1.2.3"))))
            ));


    Assertion createsPreReleaseTag = withResources(testRepository, new Initialized<>(
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
            assertionThat(new CreateTag((message, params) -> {},
                    new Anything<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                procedureThatAffects(
                    new Text("repository"),
                    () -> repo,
                    soIt(has("tag list that",
                        repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                        iterates("refs/tags/0.0.1", "refs/tags/1.2.3-foobar"))))
            ));


    Assertion createsPreReleaseTagToMatchFilter = withResources(testRepository, new Initialized<>(
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
            assertionThat(new CreateTag((message, params) -> {},
                    PRERELEASE::equals, config, new PreRelease(new Release(1, 2, 3), "foobar")),
                procedureThatAffects(
                    new Text("repository"),
                    () -> repo,
                    soIt(has("tag list that",
                        repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                        iterates("refs/tags/0.0.1", "refs/tags/1.2.3-foobar"))))
            ));


    Assertion createsNoTag = withResources(testRepository, new Initialized<>(
            c ->
                c.tag(new LambdaClosure<Void, TagConfig>(
                    this,
                    tagConfig -> {
                        tagConfig
                            .with(ReleaseType.NONE)
                            .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
                    })), gitVersionConfig),

        (repo, config) ->
            assertionThat(new CreateTag((message, params) -> {},
                    new Anything<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                procedureThatAffects(
                    new Text("repository"),
                    () -> repo,
                    soIt(has("tag list that",
                        repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                        iterates("refs/tags/0.0.1"))),
                    when(throwing(IllegalStateException.class)))
            ));


    Assertion createsNoTagDueToFilter = withResources(testRepository, new Initialized<>(
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
            assertionThat(new CreateTag((message, params) -> {},
                    new Nothing<>(), config, new PreRelease(new Release(1, 2, 3), "foobar")),
                procedureThatAffects(
                    new Text("repository"),
                    () -> repo,
                    soIt(has("tag list that",
                        repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                        iterates("refs/tags/0.0.1"))),
                    when(throwing(IllegalStateException.class)))
            ));


    Assertion doesNotCreateExistingTag = withResources(testRepository, new Initialized<>(
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
            assertionThat(new CreateTag((message, params) -> {},
                    new Anything<>(), config, new Release(0, 0, 1)),
                procedureThatAffects(
                    new Text("repository"),
                    () -> repo,
                    soIt(has("tag list that",
                        repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                        iterates("refs/tags/0.0.1"))))
            ));


}