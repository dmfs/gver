package org.dmfs.gver.git;

import org.dmfs.gver.dsl.Conditions;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.gver.dsl.utils.Tools;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.File;

import groovy.lang.Closure;

import static org.dmfs.gver.dsl.utils.Matchers.given;
import static org.dmfs.gver.dsl.utils.Qualities.present;
import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.hamcrest.matchers.function.FragileFunctionMatcher.associates;
import static org.dmfs.jems2.mockito.Mock.mock;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


class SuffixesTest
{
    ChangeTypeStrategy mStrategy = new FirstOf(
        ChangeType.MAJOR.when(new CommitMessage(new Contains("#major"))),
        ChangeType.MINOR.when(new CommitMessage(new Contains("#minor"))),
        ChangeType.PATCH.when(new CommitMessage(new Contains("#patch"))),
        ChangeType.NONE.when(new CommitMessage(new Contains("#trivial"))),
        ChangeType.UNKNOWN.when(((treeWalk, commit, branches) -> true))
    );


    @Test
    void testDefaultClean()
    {
        MatcherAssert.assertThat(new GitVersion(mStrategy, new Suffixes(), ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource("0.1.0-alpha.bundle"),
                    tempDir,
                    "main",
                    repo ->

                        associates(repo,
                            having(
                                v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                                Matchers.equalTo("0.1.0-alpha.20220116T191427Z-SNAPSHOT"))))
            ));
    }


    @Test
    void testDefaultDirty()
    {
        MatcherAssert.assertThat(new GitVersion(mStrategy, new Suffixes(), ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource("0.1.0-alpha.bundle"),
                    tempDir,
                    "main",
                    repo ->
                        given(
                            () -> {
                                new File(tempDir, "dirty").createNewFile();
                                new Git(repo).add().addFilepattern("dirty").call();
                                return true;
                            },
                            ignored ->
                                associates(repo,
                                    having(
                                        v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                                        Matchers.allOf(
                                            Matchers.matchesPattern("0\\.1\\.0-alpha.1\\.20\\d{6}T\\d{6}Z-SNAPSHOT"),
                                            having(version -> version.substring(14, 30),
                                                having(DateTime::parse,
                                                    having(DateTime::getTimestamp,
                                                        having(Number::doubleValue,
                                                            Matchers.closeTo(System.currentTimeMillis(), 1000d)))))))
                                )))));
    }


    @Test
    void testConfigured()
    {
        assertThat(new Suffixes(),
            has("with config",
                s -> {
                    s.mSuffixes.clear();
                    s.append("somesuffix");
                    return s;
                }
                ,
                has("default", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "branchName"),
                    is(present("somesuffix")))));
    }


    @Test
    void testComplexConfigured()
    {
        assertThat(new Suffixes(),
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
                allOf(has("suffix", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "branchName"),
                        is(present("somesuffix"))),
                    has("suffix", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "otherbranch"),
                        is(present("default"))))));
    }
}