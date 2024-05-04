package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.HasTagListThat;
import org.dmfs.gradle.gver.utils.Repo;
import org.dmfs.gradle.gver.utils.TestProject;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
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
import static org.saynotobugs.confidence.core.quality.Grammar.*;
import static org.saynotobugs.confidence.core.quality.Iterable.iteratesInAnyOrder;
import static org.saynotobugs.confidence.core.quality.Object.throwing;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResources;


@Confidence
class TagReleaseTaskTest
{
    Resource<File> repoDir = new TempDir();
    Resource<File> homeDir = new TempDir();

    Resource<Project> testProject = new TestProject(repoDir,
        new Strategy(
            NONE.when(new CommitMessage(new Contains("#none"))),
            MAJOR.when(new CommitMessage(new Contains("#major"))),
            MINOR.when(new CommitMessage(new Contains("#minor"))),
            PATCH.when(new CommitMessage(new Contains("#patch"))),
            UNKNOWN.when(((repository1, commit, branches) -> true))),
        homeDir);

    Assertion gitTagRelease_adds_a_next_patch_tag =
        withResources(new Repo("0.0.2-alpha.1.bundle", "main", repoDir), testProject,

            (repo, project) -> assertionThat(
                repository -> ((TagReleaseTask) project.getTasks().getByName("gitTagRelease")).perform(),
                is(procedureThatAffects(
                    new Text("alters repository"),
                    () -> repo,
                    soIt(new HasTagListThat(iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.0.2")))))));


    Assertion gitTagRelease_no_tag_on_feature_branch =
        withResources(new Repo("0.2.0-alpha.1.feature.bundle", "feature", repoDir), testProject,

            (repo, project) -> assertionThat(
                repository -> ((TagReleaseTask) project.getTasks().getByName("gitTagRelease")).perform(),
                is(procedureThatAffects(
                    new Text("preserves repository"),
                    () -> repo,
                    soIt(new HasTagListThat(iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.1.0"))),
                    when(throwing(IllegalStateException.class))))));


    Assertion gitTagRelease_skips_existing_tag =
        withResources(new Repo("0.2.0-trivial-change.bundle", "main", repoDir), testProject,

            (repo, project) -> assertionThat(
                repository -> ((TagReleaseTask) project.getTasks().getByName("gitTagRelease")).perform(),
                is(procedureThatAffects(
                    new Text("alters repository"),
                    () -> repo,
                    soIt(new HasTagListThat(iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.1.0", R_TAGS + "0.2.0")))))));
}