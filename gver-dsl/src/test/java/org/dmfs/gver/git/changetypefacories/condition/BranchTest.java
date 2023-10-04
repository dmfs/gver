package org.dmfs.gver.git.changetypefacories.condition;

import org.dmfs.gver.dsl.utils.Matches;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.saynotobugs.confidence.quality.composite.AllOf;

import static org.dmfs.jems2.mockito.Mock.mock;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.not;


class BranchTest
{
    @Test
    void test()
    {
        assertThat(new Branch("xyz"::equals),
            new AllOf<>(
                new Matches(mock(Repository.class), mock(RevCommit.class), "xyz"),
                not(new Matches(mock(Repository.class), mock(RevCommit.class), "abc"))));
    }

}