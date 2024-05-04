package org.dmfs.gver.dsl.procedure;

import org.dmfs.gver.dsl.utils.Repo;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.semver.PreRelease;
import org.dmfs.semver.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import java.io.File;

import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.saynotobugs.confidence.core.quality.Composite.has;
import static org.saynotobugs.confidence.core.quality.Grammar.soIt;
import static org.saynotobugs.confidence.core.quality.Iterable.iterates;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.withResource;

@Confidence
class CreateTagTest
{
    Resource<File> projectDir = new TempDir();
    Resource<Repository> testRepository = new Repo("0.0.2-alpha.1.bundle", "main", projectDir);

    Assertion createsReleaseTag = withResource(testRepository,
        repo -> assertionThat(new CreateTag((message, params) -> {}, new Release(1, 2, 3)),
            procedureThatAffects(
                new Text("repository"),
                () -> repo,
                soIt(has("tag list that",
                    repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                    iterates("refs/tags/0.0.1", "refs/tags/1.2.3"))))));

    Assertion createsPreReleaseTag = withResource(testRepository,
        repo -> assertionThat(new CreateTag((message, params) -> {}, new PreRelease(new Release(1, 2, 3), "foobar")),
            procedureThatAffects(
                new Text("repository"),
                () -> repo,
                soIt(has("tag list that",
                    repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                    iterates("refs/tags/0.0.1", "refs/tags/1.2.3-foobar"))))));

    Assertion doesNotCreateExistingTag = withResource(testRepository,
        repo -> assertionThat(new CreateTag((message, params) -> {}, new Release(0, 0, 1)),
            procedureThatAffects(
                new Text("repository"),
                () -> repo,
                soIt(has("tag list that",
                    repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                    iterates("refs/tags/0.0.1"))))));

}