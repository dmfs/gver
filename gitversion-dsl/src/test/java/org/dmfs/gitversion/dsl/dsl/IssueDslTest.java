package org.dmfs.gitversion.dsl.dsl;

import org.dmfs.gitversion.dsl.IssueDsl;
import org.dmfs.gitversion.dsl.IssueTracker;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import groovy.lang.Closure;

import static org.dmfs.jems2.mockito.Mock.*;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.throwing;
import static org.saynotobugs.confidence.quality.Core.*;


class IssueDslTest
{
    @Test
    void testMatch()
    {
        Closure<Predicate<Object>> mockClosure = mock(Closure.class);
        IssueTracker mockTracker = mock(IssueTracker.class,
            with(mt -> mt.containsIssue(mockClosure), returning("match"::equals)));
        assertThat(new IssueDsl(Optional.of(mockTracker)),
            has("issue", dsl -> dsl.isIssue(mockClosure), is(
                allOf(
                    satisfiedBy("match"),
                    not(satisfiedBy("anything"))))));
    }


    @Test
    void testMisconfigured()
    {
        assertThat(new IssueDsl(Optional.empty()),
            has("is issue", dsl -> () -> dsl.isIssue(mock(Closure.class)), is(throwing(instanceOf(NoSuchElementException.class)))));
    }
}