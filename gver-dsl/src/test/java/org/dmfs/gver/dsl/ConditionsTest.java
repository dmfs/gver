package org.dmfs.gver.dsl;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.dmfs.gver.dsl.Conditions.contains;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


class ConditionsTest
{
    @Test
    void testAnyThat()
    {
        assertThat(Conditions.anyThat("123"::equals, "abc"::equals),
            allOf(
                satisfiedBy(new HashSet<>(asList("abc"))),
                satisfiedBy(new HashSet<>(asList("123"))),
                satisfiedBy(new HashSet<>(asList("abc", "xyz"))),
                satisfiedBy(new HashSet<>(asList("ert", "123", "xyz"))),
                not(satisfiedBy(new HashSet<>(asList("abcd", "xyz")))),
                not(satisfiedBy(new HashSet<String>()))));
    }


    @Test
    void testNoneThat()
    {
        assertThat(Conditions.noneThat("123"::equals, "abc"::equals),
            allOf(
                not(satisfiedBy(new HashSet<>(asList("abc")))),
                not(satisfiedBy(new HashSet<>(asList("123")))),
                not(satisfiedBy(new HashSet<>(asList("abc", "xyz")))),
                not(satisfiedBy(new HashSet<>(asList("ert", "123", "xyz")))),
                satisfiedBy(new HashSet<>(asList("abcd", "xyz"))),
                satisfiedBy(new HashSet<String>())));
    }


    @Test
    void testOnly()
    {
        assertThat(Conditions.only("123"::equals, "abc"::equals),
            allOf(
                satisfiedBy(new HashSet<>(asList("abc"))),
                satisfiedBy(new HashSet<>(asList("123"))),
                satisfiedBy(new HashSet<>(asList("abc", "123"))),
                not(satisfiedBy(new HashSet<>(asList("abc", "xyz")))),
                not(satisfiedBy(new HashSet<>(asList("abc", "123", "xyz")))),
                not(satisfiedBy(new HashSet<>(asList("abcd", "xyz")))),
                satisfiedBy(new HashSet<String>())));
    }


    @Test
    void testContains()
    {
        assertThat(Conditions.contains(Pattern.compile("abc")),
            allOf(
                satisfiedBy("abc"),
                satisfiedBy("xabcy"),
                satisfiedBy("aaaaabcabc"),
                not(satisfiedBy("abac")),
                not(satisfiedBy("ab"))));
    }


    @Test
    void testMatches()
    {
        assertThat(Conditions.matches(Pattern.compile("abc")),
            allOf(
                satisfiedBy("abc"),
                not(satisfiedBy("xabcy")),
                not(satisfiedBy("aaaaabcabc")),
                not(satisfiedBy("abac")),
                not(satisfiedBy("ab"))));
    }
}