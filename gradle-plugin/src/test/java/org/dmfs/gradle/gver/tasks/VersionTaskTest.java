package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.Repo;
import org.dmfs.gradle.gver.utils.TestProject;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.jems2.Generator;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.Project;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.Resources;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResources;
import static org.saynotobugs.confidence.quality.Core.*;


@Confidence
class VersionTaskTest
{
    Resource<File> repoDir = new TempDir();
    Resource<File> homeDir = new TempDir();

    Resource<Repository> testRepository = new Repo("0.0.2-alpha.1.bundle", "main", repoDir);

    Resource<Project> testProject = new TestProject(repoDir,
        new Strategy(
            NONE.when(new CommitMessage(new Contains("#none"))),
            MAJOR.when(new CommitMessage(new Contains("#major"))),
            MINOR.when(new CommitMessage(new Contains("#minor"))),
            PATCH.when(new CommitMessage(new Contains("#patch"))),
            UNKNOWN.when(((repository1, commit, branches) -> true))),
        homeDir);

    Assertion gitVersion_prints_version =
        withResources(
            Resources.systemOut(), testRepository,
            (systemOut, repo) ->
                assertionThat(repository -> ((VersionTask) testProject.value().getTasks().getByName("gitVersion")).perform(),
                    is(procedureThatAffects(
                        new Text("System.out"),
                        () -> systemOut,
                        soIt(has(
                            "output",
                            Generator::next,
                            containsPattern("0\\.0\\.2-alpha\\.1\\.20220116T202206Z-SNAPSHOT")))))));
}