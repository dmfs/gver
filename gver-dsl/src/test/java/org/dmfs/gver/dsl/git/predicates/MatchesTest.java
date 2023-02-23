package org.dmfs.gver.dsl.git.predicates;

import org.dmfs.gver.git.predicates.Matches;
import org.junit.jupiter.api.Test;

import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


class MatchesTest
{
    @Test
    void test()
    {
        assertThat(new Matches("123"),
            is(allOf(
                satisfiedBy("123"),
                not(satisfiedBy("1 23")),
                not(satisfiedBy("abc123xyz")),
                not(satisfiedBy("123xyz")),
                not(satisfiedBy("a1b2c3")),
                not(satisfiedBy("")))));
    }

}