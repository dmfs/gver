package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.Repository;
import org.dmfs.gradle.gver.utils.TestProject;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.Suffixes;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.jems2.FragileFunction;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.Present;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.eclipse.jgit.lib.Constants.R_TAGS;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.*;
import static org.saynotobugs.confidence.quality.Core.*;


@Confidence
class TagTaskTest
{

    Assertion gitTagRelease_adds_tag_for_current_version =
        withResources("with temporary directory and git repository",
            new TempDir(),
            new Repository(getClass().getClassLoader().getResource("0.0.2-alpha.1.bundle"), "main"),

            // we use a separate gradle home dir to make this test pass
            (tempDir, repo) -> withResources("temporary user home dir and project",
                new TempDir(),
                (FragileFunction<File, WithResource.Resource<Project>, Exception>) new TestProject(tempDir,
                    new Strategy(
                        MAJOR.when(new CommitMessage(new Contains("#major"))),
                        MINOR.when(new CommitMessage(new Contains("#minor"))),
                        PATCH.when(new CommitMessage(new Contains("#patch"))),
                        UNKNOWN.when(((repository1, commit, branches) -> true))),
                    new Suffixes((repository12, commit, branch) -> new Present<>("-SNAPSHOT"))),

                (userHome, project) -> assertionThat(repository -> ((TagTask) project.getTasks().getByName("gitTag")).perform(),
                    is(procedureThatAffects(
                        new Text("alters repository"),
                        () -> repo,
                        soIt(has(repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                            iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.0.2-alpha.1-SNAPSHOT"))))))));


    Assertion gitTagRelease_fails_on_dirty_repo =
        withResource("with temporary directory and git repository",
            new TempDir(),
            tempDir -> withResource(initialized(repo -> {
                        new File(tempDir, "newfile").createNewFile();
                        new Git(repo).add().addFilepattern("newfile").call();
                    },
                    () -> new Repository(getClass().getClassLoader().getResource("0.0.2-alpha.1.bundle"), "main").value(tempDir)),

                // we use a separate gradle home dir to make this test pass
                repo -> withResources("temporary user home dir and project",
                    new TempDir(),
                    (FragileFunction<File, WithResource.Resource<Project>, Exception>) new TestProject(tempDir,
                        new Strategy(
                            MAJOR.when(new CommitMessage(new Contains("#major"))),
                            MINOR.when(new CommitMessage(new Contains("#minor"))),
                            PATCH.when(new CommitMessage(new Contains("#patch"))),
                            UNKNOWN.when(((repository1, commit, branches) -> true)))),

                    (userHome, project) ->
                        assertionThat(repository -> ((TagTask) project.getTasks().getByName("gitTag")).perform(),
                            is(procedureThatAffects(
                                new Text("alters repository"),
                                () -> repo,
                                soIt(has(repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                                    // same tags as before
                                    iteratesInAnyOrder(R_TAGS + "0.0.1"))),
                                when(throwing(GradleException.class))))))));

}