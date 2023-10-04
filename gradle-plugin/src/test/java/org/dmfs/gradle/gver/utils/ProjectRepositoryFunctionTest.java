package org.dmfs.gradle.gver.utils;

import org.dmfs.gver.dsl.Strategy;
import org.eclipse.jgit.lib.Repository;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;

import java.io.File;

import static org.dmfs.jems2.confidence.Jems2.maps;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.*;
import static org.saynotobugs.confidence.quality.Core.equalTo;
import static org.saynotobugs.confidence.quality.Core.has;


@Confidence
class ProjectRepositoryFunctionTest
{
    Assertion provides_right_directory = withResource(
        tempDir(),
        tempDir -> withResource(new TestProject(tempDir, new Strategy()),
            project -> assertionThat(ProjectRepositoryFunction.INSTANCE,
                maps(
                    project,
                    has("directory", Repository::getDirectory, equalTo(new File(tempDir, ".git")))))));
}