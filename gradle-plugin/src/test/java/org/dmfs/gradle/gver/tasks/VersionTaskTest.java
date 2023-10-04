package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.Repository;
import org.dmfs.gradle.gver.utils.TestProject;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.jems2.FragileFunction;
import org.dmfs.jems2.Generator;
import org.gradle.api.Project;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;

import java.io.File;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.*;
import static org.saynotobugs.confidence.quality.Core.*;


@Confidence
class VersionTaskTest
{

    Assertion gitVersion_prints_version = withResource(systemOut(),
        systemOut -> withResources("with temporary directory and git repository",
            tempDir(),
            new Repository(getClass().getClassLoader().getResource("0.0.2-alpha.1.bundle"), "main"),

            // we use a separate gradle home dir to make this test pass
            (tempDir, repo) -> withResources("temporary user home dir and project",
                tempDir(),
                (FragileFunction<File, WithResource.Resource<Project>, Exception>) new TestProject(tempDir,
                    new Strategy(
                        MAJOR.when(new CommitMessage(new Contains("#major"))),
                        MINOR.when(new CommitMessage(new Contains("#minor"))),
                        PATCH.when(new CommitMessage(new Contains("#patch"))),
                        UNKNOWN.when(((repository1, commit, branches) -> true)))),

                (userHome, project) -> assertionThat(repository -> ((VersionTask) project.getTasks().getByName("gitVersion")).perform(),
                    is(procedureThatAffects(
                        new Text("System.out"),
                        () -> systemOut,
                        soIt(has(
                            "output",
                            Generator::next,
                            containsPattern("0\\.0\\.2-alpha\\.1\\.20220116T202206Z-SNAPSHOT")))))))));
}