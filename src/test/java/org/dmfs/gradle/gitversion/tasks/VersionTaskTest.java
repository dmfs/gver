package org.dmfs.gradle.gitversion.tasks;

import org.dmfs.gradle.gitversion.dsl.GitVersionConfig;
import org.dmfs.gradle.gitversion.dsl.Strategy;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gradle.gitversion.git.predicates.Contains;
import org.dmfs.jems2.Generator;
import org.dmfs.jems2.Procedure;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.dmfs.gradle.gitversion.git.ChangeType.*;
import static org.dmfs.gradle.gitversion.utils.Matchers.given;
import static org.dmfs.gradle.gitversion.utils.StdOutCaptured.stdoutCaptured;
import static org.dmfs.gradle.gitversion.utils.Tools.withRepository;
import static org.dmfs.gradle.gitversion.utils.Tools.withTempFolder;
import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.hamcrest.matchers.procedure.ProcedureMatcher.processes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;


class VersionTaskTest
{

    @Test
    void test()
    {
        assertThat((Procedure<Project>) project -> {
                try
                {
                    ((VersionTask) project.getTasks().getByName("gitVersion")).perform();
                }
                catch (Exception e)
                {
                    throw new AssertionError("Unexpected Exception", e);
                }
            },
            stdoutCaptured(stdProvider ->
                withTempFolder(tempDir ->
                    withRepository(getClass().getClassLoader().getResource("0.0.2-alpha.1.bundle"),
                        tempDir,
                        "main",
                        repository ->
                            given(
                                () ->
                                {
                                    Project p = ProjectBuilder.builder().withProjectDir(tempDir).build();
                                    p.getPluginManager().apply("org.dmfs.gitversion");
                                    ((GitVersionConfig) p.getExtensions().getByName("gitVersion")).mChangeTypeStrategy = new Strategy();
                                    ((GitVersionConfig) p.getExtensions().getByName("gitVersion")).mChangeTypeStrategy.mChangeTypeStrategies.addAll(
                                        asList(
                                            MAJOR.when(new CommitMessage(new Contains("#major"))),
                                            MINOR.when(new CommitMessage(new Contains("#minor"))),
                                            PATCH.when(new CommitMessage(new Contains("#patch"))),
                                            UNKNOWN.when(((commit, branches) -> true))));
                                    return p;
                                },
                                project -> having(
                                    "stdout",
                                    proc -> repo -> proc.process(project),
                                    processes(() -> stdProvider,
                                        having(Generator::next, containsString("0.0.2-alpha.1"))
                                    )))))));

    }

}