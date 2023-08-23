package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.StdOutCaptured;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.jems2.Generator;
import org.dmfs.jems2.Procedure;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.dmfs.gradle.gver.utils.Matchers.given;
import static org.dmfs.gradle.gver.utils.Tools.withRepository;
import static org.dmfs.gradle.gver.utils.Tools.withTempFolder;
import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.hamcrest.matchers.procedure.ProcedureMatcher.processes;
import static org.hamcrest.Matchers.containsString;


class VersionTaskTest
{

    @Test
    void test()
    {
        MatcherAssert.assertThat((Procedure<Project>) project -> {
                try
                {
                    ((VersionTask) project.getTasks().getByName("gitVersion")).perform();
                }
                catch (Exception e)
                {
                    throw new AssertionError("Unexpected Exception", e);
                }
            },
            StdOutCaptured.stdoutCaptured(stdProvider ->
                withTempFolder(
                    userHome ->
                        withTempFolder(tempDir ->
                            withRepository(getClass().getClassLoader().getResource("0.0.2-alpha.1.bundle"),
                                tempDir,
                                "main",
                                repository ->
                                    given(
                                        () ->
                                        {
                                            Project p = ProjectBuilder.builder()
                                                .withProjectDir(tempDir)
                                                .withGradleUserHomeDir(userHome)
                                                .build();
                                            p.getPluginManager().apply("org.dmfs.gver");
                                            ((GitVersionConfig) p.getExtensions().getByName("gver")).mChangeTypeStrategy = new Strategy();
                                            ((GitVersionConfig) p.getExtensions().getByName("gver")).mChangeTypeStrategy.mChangeTypeStrategies.addAll(
                                                asList(
                                                    MAJOR.when(new CommitMessage(new Contains("#major"))),
                                                    MINOR.when(new CommitMessage(new Contains("#minor"))),
                                                    PATCH.when(new CommitMessage(new Contains("#patch"))),
                                                    UNKNOWN.when(((repository1, commit, branches) -> true))));
                                            return p;
                                        },
                                        project -> having(
                                            "stdout",
                                            proc -> repo -> proc.process(project),
                                            processes(() -> stdProvider,
                                                having(Generator::next, containsString("0.0.2-alpha.1.20220116T202206Z-SNAPSHOT"))
                                            ))))))));

    }

}