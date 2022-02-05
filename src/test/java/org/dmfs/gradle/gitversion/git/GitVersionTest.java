package org.dmfs.gradle.gitversion.git;

import org.dmfs.gradle.gitversion.git.changetypefacories.FirstOf;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gradle.gitversion.git.predicates.Contains;
import org.dmfs.jems2.single.Unchecked;
import org.dmfs.semver.VersionSequence;
import org.gradle.internal.impldep.com.google.common.io.Files;
import org.junit.Rule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;

import static org.dmfs.gradle.gitversion.git.ChangeType.*;
import static org.dmfs.gradle.gitversion.utils.Tools.withRepository;
import static org.dmfs.gradle.gitversion.utils.Tools.withTempFolder;
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
        UNKNOWN.when(((commit, branches) -> true))
    );

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @ParameterizedTest
    @ValueSource(strings = {
        "0.0.1-alpha", "0.0.1-alpha.1", "0.0.1-alpha.2", "0.0.1", "0.0.2-alpha", "0.0.2-alpha.1", "0.1.0-alpha",
        "0.1.0-alpha.2b", "0.1.0-alpha.3b", "0.1.0-alpha.4b", "0.1.0", "0.2.0-alpha.feature", "0.2.0-alpha.1.feature",
        "0.2.0-alpha.2.feature", "0.1.1-alpha.feature", "0.1.1-annotated", "0.2.0-alpha.3.merge", "0.2.0" })
    void testMainNew(String bundle)
    {
        assertThat(new GitVersion(mStrategy),
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
        assertThat(new GitVersion(mStrategy),
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
}