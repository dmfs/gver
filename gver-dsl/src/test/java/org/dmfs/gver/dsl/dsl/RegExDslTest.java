package org.dmfs.gver.dsl.dsl;

import org.dmfs.gver.dsl.IssueTracker;
import org.dmfs.gver.dsl.RegExDsl;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Closure;

import static org.dmfs.jems2.mockito.Mock.mock;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


public final class RegExDslTest
{
    @Test
    void test()
    {
        IssueTracker mockIssueTracker = mock(IssueTracker.class);
        Closure<Predicate<String>> mockClosure = new Closure<Predicate<String>>(this)
        {
            @Override
            public Predicate<String> call()
            {
                return "456"::equals;
            }
        };
        Matcher matching = Pattern.compile("123#(?<gr>\\d+)").matcher("123#456");
        matching.matches();
        Matcher nonMatching = Pattern.compile("123#(?<gr>\\d+)").matcher("123#987");
        nonMatching.matches();

        assertThat(new RegExDsl(Optional.of(mockIssueTracker)),
            has("where", dsl -> dsl.where("gr", mockClosure),
                allOf(satisfiedBy(matching),
                    not(satisfiedBy(nonMatching)))));
    }
}
