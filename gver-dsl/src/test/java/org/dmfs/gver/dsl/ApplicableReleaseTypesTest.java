
package org.dmfs.gver.dsl;

import org.dmfs.gver.dsl.utils.LambdaClosure;
import org.dmfs.gver.dsl.utils.Repo;
import org.dmfs.jems2.Procedure;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.resource.Initialized;
import org.saynotobugs.confidence.junit5.engine.resource.LazyResource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.jems2.mockito.Mock.mock;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResources;
import static org.saynotobugs.confidence.quality.Core.iterates;

@Confidence
class ApplicableReleaseTypesTest
{
    Resource<File> projectDir = new TempDir();
    Resource<Repository> testRepository = new Repo("0.0.2-alpha.1.bundle", "main", projectDir);
    Resource<GitVersionConfig> config = new Initialized<>(config ->
        config.tag(new LambdaClosure<Void, TagConfig>(
            this,
            tagConfig -> {
                tagConfig
                    .with(ReleaseType.RELEASE)
                    .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                tagConfig
                    .with(ReleaseType.PRERELEASE)
                    .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("feat"::equals)));
                tagConfig
                    .with(ReleaseType.PRERELEASE)
                    .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("main"::equals)));
                tagConfig
                    .with(ReleaseType.NONE)
                    .when(new LambdaClosure<Void, Conditions>(this, (Procedure<Conditions>) condition -> condition.branch("protected"::equals)));
            })),
        new LazyResource<>(GitVersionConfig::new, config -> {}));


    Assertion none_apply = withResources(testRepository, config,
        (repo, conf) ->
            assertionThat(new ApplicableReleaseTypes(conf, repo, mock(RevCommit.class), "branch"),
                iterates()));


    Assertion release_and_prerelease_apply = withResources(testRepository, config,
        (repo, conf) ->
            assertionThat(new ApplicableReleaseTypes(conf, repo, mock(RevCommit.class), "main"),
                iterates(ReleaseType.RELEASE, ReleaseType.PRERELEASE)));


    Assertion prerelease_applies = withResources(testRepository, config,
        (repo, conf) ->
            assertionThat(new ApplicableReleaseTypes(conf, repo, mock(RevCommit.class), "feat"),
                iterates(ReleaseType.PRERELEASE)));


    Assertion nothing_applies = withResources(testRepository, config,
        (repo, conf) ->
            assertionThat(new ApplicableReleaseTypes(conf, repo, mock(RevCommit.class), "protected"),
                iterates(ReleaseType.NONE)));

}