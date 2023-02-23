package org.dmfs.gver.dsl.git;

import com.google.common.io.Files;

import org.dmfs.gver.git.ChangeTypeStrategy;
import org.dmfs.gver.git.GitVersion;
import org.dmfs.gver.git.Suffixes;
import org.dmfs.gver.git.WithoutBuildMeta;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.gver.git.changetypefacories.condition.Affects;
import org.dmfs.gver.dsl.utils.Tools;
import org.dmfs.gver.git.*;
import org.dmfs.jems2.single.Unchecked;
import org.dmfs.semver.VersionSequence;
import org.eclipse.jgit.api.Git;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.Charset;

import static org.dmfs.gver.dsl.utils.Matchers.given;
import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.hamcrest.matchers.function.FragileFunctionMatcher.associates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


class GitVersionTest
{
    ChangeTypeStrategy mStrategy = new FirstOf(
        ChangeType.MAJOR.when(new CommitMessage(new Contains("#major"))),
        ChangeType.MINOR.when(new CommitMessage(new Contains("#minor"))),
        ChangeType.PATCH.when(new CommitMessage(new Contains("#patch"))),
        ChangeType.NONE.when(new CommitMessage(new Contains("#trivial"))),
        ChangeType.UNKNOWN.when(((treeWalk, commit, branches) -> true))
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
        MatcherAssert.assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
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
        MatcherAssert.assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
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
        MatcherAssert.assertThat(new GitVersion(mStrategy, mSuffixes, b -> b + "-b"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
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
        ChangeType.MINOR.when(new Affects(set -> set.stream().anyMatch(f -> f.endsWith("t.important")))),
        ChangeType.NONE.when(new Affects(set -> set.stream().anyMatch(f -> f.endsWith("t.ignore")))),
        ChangeType.MAJOR.when((repository, commit, branch) -> true)
    );


    @ParameterizedTest
    @ValueSource(strings = { "0.1.0.trivial-update", "0.2.0-alpha.nontrivial-update" })
    void testAffects(String bundle)
    {
        MatcherAssert.assertThat(new GitVersion(mAffectsStrategy, mSuffixes, ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
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
    @CsvSource({
        "0.1.0-alpha, 0.1.0-alpha.1",
        "0.2.0,       0.3.0-alpha" })
    void testDirtyNewFile(String bundle, String version)
    {
        MatcherAssert.assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
                    tempDir,
                    "main",
                    repo ->
                        given(
                            () -> new File(tempDir, "dirty").createNewFile(),
                            ignored ->
                                associates(repo,
                                    having(
                                        v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                                        equalTo(version))
                                )))));
    }


    @ParameterizedTest
    @CsvSource({
        "0.1.0-alpha, 0.1.0-alpha.1",
        "0.2.0,       0.3.0-alpha" })
    void testDirtyChanged(String bundle, String version)
    {
        MatcherAssert.assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
                    tempDir,
                    "main",
                    repo ->
                        given(
                            () -> new File(tempDir, "version").delete(),
                            ignored ->
                                associates(repo,
                                    having(
                                        v -> new VersionSequence(new WithoutBuildMeta(v)).toString(),
                                        equalTo(version))
                                )))));
    }


    @ParameterizedTest
    @CsvSource({
        "0.1.0-alpha, 0.1.0-alpha.1",
        "0.2.0,       0.3.0-alpha" })
    void testDirtyStaged(String bundle, String version)
    {
        MatcherAssert.assertThat(new GitVersion(mStrategy, mSuffixes, ignored -> "alpha"),
            Tools.withTempFolder(tempDir ->
                Tools.withRepository(getClass().getClassLoader().getResource(bundle + ".bundle"),
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
                                        equalTo(version))
                                )))));
    }

}