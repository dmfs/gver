package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.HasTagListThat;
import org.dmfs.gradle.gver.utils.Repo;
import org.dmfs.gradle.gver.utils.TestProject;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.Suffixes;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.jems2.optional.Present;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.eclipse.jgit.lib.Constants.R_TAGS;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResource;
import static org.saynotobugs.confidence.junit5.engine.Resources.initialized;
import static org.saynotobugs.confidence.quality.Core.*;


@Confidence
class TagTaskTest
{
    Resource<File> projectDir = new TempDir();
    Resource<File> homeDir = new TempDir();
    Resource<Repository> testRepository = new Repo("0.0.2-alpha.1.bundle", "main", projectDir);
    Resource<Project> testProject = new TestProject(projectDir,
        new Strategy(
            MAJOR.when(new CommitMessage(new Contains("#major"))),
            MINOR.when(new CommitMessage(new Contains("#minor"))),
            PATCH.when(new CommitMessage(new Contains("#patch"))),
            UNKNOWN.when(((repository1, commit, branches) -> true))),
        new Suffixes((repository12, commit, branch) -> new Present<>("-SNAPSHOT")),
        homeDir);


    Assertion gitTagRelease_adds_tag_for_current_version =
        withResource(testProject,
            project -> assertionThat(repository -> ((TagTask) project.getTasks().getByName("gitTag")).perform(),
                is(procedureThatAffects(
                    new Text("alters repository"),
                    () -> testRepository.value(),
                    soIt(new HasTagListThat(iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.0.2-alpha.1-SNAPSHOT")))))));


    Assertion gitTagRelease_fails_on_dirty_repo =
        withResource(initialized(
                repo -> {
                    new File(repo.getWorkTree(), "newfile").createNewFile();
                    new Git(repo).add().addFilepattern("newfile").call();
                },
                testRepository),

            repo -> assertionThat(repository -> ((TagTask) testProject.value().getTasks().getByName("gitTag")).perform(),
                is(procedureThatAffects(
                    new Text("alters repository"),
                    () -> repo,
                    soIt(new HasTagListThat(iteratesInAnyOrder(R_TAGS + "0.0.1"))), // same tags as before
                    when(throwing(GradleException.class))))));

}