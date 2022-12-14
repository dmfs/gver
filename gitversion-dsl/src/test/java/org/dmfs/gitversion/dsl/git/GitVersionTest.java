package org.dmfs.gitversion.dsl.git;

import com.google.common.io.Files;

import org.dmfs.gitversion.git.ChangeTypeStrategy;
import org.dmfs.gitversion.git.GitVersion;
import org.dmfs.gitversion.git.Suffixes;
import org.dmfs.gitversion.git.WithoutBuildMeta;
import org.dmfs.gitversion.git.changetypefacories.FirstOf;
import org.dmfs.gitversion.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gitversion.git.predicates.Contains;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.Affects;
import org.dmfs.jems2.single.Unchecked;
import org.dmfs.semver.VersionSequence;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.Charset;

import static org.dmfs.gitversion.dsl.utils.Tools.withRepository;
import static org.dmfs.gitversion.dsl.utils.Tools.withTempFolder;
import static org.dmfs.gitversion.git.ChangeType.*;
import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.hamcrest.matchers.function.FragileFunctionMatcher.associates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


class GitVersionTest
{
    ChangeTypeStrategy mStrategy = new FirstOf(
        MAJOR.when(new CommitMessage(new Contains("#major"))),
        MINOR.when(new CommitMessage(new Contains("#minor"))),
        PATCH.when(new CommitMessage(new Contains("#patch"))),
        NONE.when(new CommitMessage(new Contains("#trivial"))),
        UNKNOWN.when(((treeWalk, commit, branches) -> true))
    );

    Suffixes mSuffixes = new Suffixes();

    {
        mSuffixes.mSuffixes.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0.0.1-alpha", "0.0.1-alpha.1", "0.0.1-alpha.2", "0.0.1", "0.0.2-alpha", "0.0.2-alpha.1", "0.1.0-alpha",
        "0.1.0-alpha.2b", "0.1.0-alpha.3b", "0.1.0-alpha.4b", "0.1.0", "0.2.0-alpha.feature", "0.2.0-alpha.1.feature",
        "0.2.0-alpha.2.feature", "0.1.1-alpha.feature", "0.1.1-annotated", "0.2.0-alpha.3.merge", "0.2.0", "0.2.0-trivial-change" })
    void testMainNew(String bundle)
    {
        assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            withTempFolder(tempDir ->
                withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
                    tempDir,
                    "main",
                    repo -> associates(repo,
                        having(
                            v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                            equalTo(
                                new Unchecked<>(() -> Files.asCharSource(new File(tempDir, "version"), Charset.defaultCharset()).read()).value().trim()))
                    ))));
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "0.2.0-alpha.feature", "0.2.0-alpha.1.feature", "0.2.0-alpha.2.feature", "0.1.1-alpha.feature",
        "0.2.0-alpha.3.merge" })
    void testFeature(String bundle)
    {
        assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            withTempFolder(tempDir ->
                withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
                    tempDir,
                    "feature",
                    repo -> associates(repo,
                        having(
                            v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                            equalTo(
                                new Unchecked<>(() -> Files.asCharSource(new File(tempDir, "version"), Charset.defaultCharset()).read()).value().trim()))
                    ))));
    }


    @ParameterizedTest
    @CsvSource({ "0.2.0-main-b,main", "0.2.0-main-b.1,main", "0.2.0-alpha-b.2,alpha" })
    void testAlphaConfigFeature(String bundle, String branch)
    {
        assertThat(new GitVersion(mStrategy, mSuffixes, b -> b + "-b"),
            withTempFolder(tempDir ->
                withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
                    tempDir,
                    branch,
                    repo -> associates(repo,
                        having(
                            v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                            equalTo(
                                new Unchecked<>(() -> Files.asCharSource(new File(tempDir, "version"), Charset.defaultCharset()).read()).value().trim()))
                    ))));
    }


    ChangeTypeStrategy mAffectsStrategy = new FirstOf(
        MINOR.when(new Affects(set -> set.stream().anyMatch(f -> f.endsWith("t.important")))),
        NONE.when(new Affects(set -> set.stream().anyMatch(f -> f.endsWith("t.ignore")))),
        MAJOR.when((repository, commit, branch) -> true)
    );


    @ParameterizedTest
    @ValueSource(strings = { "0.1.0.trivial-update", "0.2.0-alpha.nontrivial-update" })
    void testAffects(String bundle)
    {
        assertThat(new GitVersion(mAffectsStrategy, mSuffixes, ignored -> "alpha"),
            withTempFolder(tempDir ->
                withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
                    tempDir,
                    "main",
                    repo -> associates(repo,
                        having(
                            v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                            equalTo(
                                new Unchecked<>(() -> Files.asCharSource(new File(tempDir, "version"), Charset.defaultCharset()).read()).value().trim()))
                    ))));
    }

}