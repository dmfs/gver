package org.dmfs.gver.git.changetypefacories.condition;

import org.dmfs.gver.dsl.utils.Matches;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import static org.dmfs.jems2.mockito.Mock.mock;
import static org.saynotobugs.confidence.Assertion.assertThat;

/**
 * Test {@link EnvVariable} for a couple of variables set in the build.gradle file.
 */
class EnvVariableTest
{
    @Test
    void testFooBar()
    {
        assertThat(new EnvVariable("GVER_TEST_VARIABLE_FOOBAR", "foobar"::equals),
            new Matches(mock(Repository.class), mock(RevCommit.class), "branch"));
    }

    @Test
    void testEmpty()
    {
        assertThat(new EnvVariable("GVER_TEST_VARIABLE_EMPTY", String::isEmpty),
            new Matches(mock(Repository.class), mock(RevCommit.class), "branch"));
    }

    @Test
    void testAbsent()
    {
        assertThat(new EnvVariable("GVER_TEST_VARIABLE_ABSENT", String::isEmpty),
            new Matches(mock(Repository.class), mock(RevCommit.class), "branch"));
    }
}