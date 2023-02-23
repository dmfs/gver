package org.dmfs.gradle.gver.utils;

import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.dmfs.gradle.gver.utils.Tools.withTempFolder;
import static org.dmfs.gradle.gver.utils.Tools.withTestProject;
import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.hamcrest.matchers.function.FragileFunctionMatcher.associates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


class ProjectRepositoryFunctionTest
{
    @Test
    void test()
    {
        assertThat(
            ProjectRepositoryFunction.INSTANCE,
            withTempFolder(tempDir ->
                withTestProject(tempDir,
                    project -> associates(
                        project,
                        having(Repository::getDirectory, is(new File(tempDir, ".git")))))));
    }
}