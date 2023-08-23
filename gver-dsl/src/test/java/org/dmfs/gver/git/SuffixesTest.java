package org.dmfs.gver.git;

import groovy.lang.Closure;
import org.dmfs.gver.dsl.Conditions;
import org.dmfs.gver.dsl.utils.Repository;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.semver.Version;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;
import java.net.URL;

import static org.dmfs.jems2.confidence.Jems2.maps;
import static org.dmfs.jems2.confidence.Jems2.present;
import static org.dmfs.jems2.mockito.Mock.mock;
import static org.dmfs.semver.confidence.SemVer.preRelease;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.*;
import static org.saynotobugs.confidence.quality.Core.*;


@Confidence
class SuffixesTest
{
    URL repo = getClass().getClassLoader().getResource("0.1.0-alpha.bundle");

    ChangeTypeStrategy mStrategy = new FirstOf(
        ChangeType.MAJOR.when(new CommitMessage(new Contains("#major"))),
        ChangeType.MINOR.when(new CommitMessage(new Contains("#minor"))),
        ChangeType.PATCH.when(new CommitMessage(new Contains("#patch"))),
        ChangeType.NONE.when(new CommitMessage(new Contains("#trivial"))),
        ChangeType.UNKNOWN.when(((treeWalk, commit, branches) -> true))
    );

    Assertion default_strategy_on_clean_repo = withResources("", new TempDir(), new Repository(repo, "main"),

        (tempDir, repo) -> assertionThat(
            new GitVersion(mStrategy, new Suffixes(), ignored -> "alpha"),
            maps(repo, to(preRelease(0, 1, 0, "alpha.20220116T191427Z-SNAPSHOT")))));


    Assertion default_strategy_on_dirty_repo = withResources("", new TempDir(), new Repository(repo, "main"),

        // TODO: replace with initialized resource
        (tempDir, repo) -> withResource(() -> {
                new File(tempDir, "dirty").createNewFile();
                new Git(repo).add().addFilepattern("dirty").call();
                return new WithResource.Resource<org.eclipse.jgit.lib.Repository>()
                {
                    @Override
                    public void close()
                    {
                        repo.close();
                    }

                    @Override
                    public org.eclipse.jgit.lib.Repository value()
                    {

                        return repo;
                    }
                };
            },
            r -> assertionThat(new GitVersion(mStrategy, new Suffixes(), ignored -> "alpha"),
                maps(repo, to(
                    has(
                        (Version v) -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                        allOf(
                            matchesPattern("0\\.1\\.0-alpha.1\\.20\\d{6}T\\d{6}Z-SNAPSHOT"),
                            has((String version) -> version.substring(14, 30),
                                has((String s) -> DateTime.parse(s),
                                    has(DateTime::getTimestamp,
                                        has(Number::doubleValue,
                                            closeTo(System.currentTimeMillis(), 1000d)))))))
                )))));


    Assertion test_suffixes_with_simple_config =
        assertionThat(
            new Suffixes(),
            has("with config",
                s -> {
                    s.mSuffixes.clear();
                    s.append("somesuffix");
                    return s;
                },
                has("default", s -> s.suffix(mock(org.eclipse.jgit.lib.Repository.class), mock(RevCommit.class), "branchName"),
                    is(present("somesuffix")))));

    Assertion test_suffixes_with_complex_config =
        assertionThat(
            new Suffixes(),
            has("with config",
                s -> {
                    s.mSuffixes.clear();
                    s.append("somesuffix").when(
                        new Closure<Object>(s)
                        {
                            @Override
                            public Object call()
                            {
                                ((Conditions) getDelegate()).branch("branchName"::equals);
                                return null;
                            }
                        }
                    );
                    s.append("default");
                    return s;
                },
                allOf(has("suffix", s -> s.suffix(mock(org.eclipse.jgit.lib.Repository.class), mock(RevCommit.class), "branchName"),
                        is(present("somesuffix"))),
                    has("suffix", s -> s.suffix(mock(org.eclipse.jgit.lib.Repository.class), mock(RevCommit.class), "otherbranch"),
                        is(present(("default")))))));
}