package org.dmfs.gitversion.dsl.git.predicates;

import org.dmfs.gitversion.git.predicates.Contains;
import org.junit.jupiter.api.Test;

import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


class ContainsTest
{
    @Test
    void test()
    {
        assertThat(new Contains("123"),
            is(allOf(
                satisfiedBy("123"),
                satisfiedBy("abc123xyz"),
                satisfiedBy("123xyz"),
                satisfiedBy("abc123"),
                not(satisfiedBy("1 23")),
                not(satisfiedBy("a1b2c3")),
                not(satisfiedBy("")))));
    }
}