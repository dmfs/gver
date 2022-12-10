package org.dmfs.gitversion.dsl.dsl.issuetracker;

import org.dmfs.gitversion.dsl.issuetracker.GitHub;
import org.dmfs.jems2.FragileBiFunction;
import org.dmfs.jems2.FragileFunction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import groovy.lang.Closure;

import static org.dmfs.jems2.hamcrest.matchers.LambdaMatcher.having;
import static org.dmfs.jems2.mockito.Mock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


class GitHubTest
{

    @Test
    void testMatch()
    {
        FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> mockIssueFunction =
            mock(FragileBiFunction.class,
                with(f -> f.value(eq("x/y"), any()), returning(
                    mock(FragileFunction.class,
                        with(f -> f.value(any()), throwing(new AssertionError("unexpected call"))),
                        with(f -> f.value(123), returning("{}")))
                )));

        assertThat(new GitHub(mockIssueFunction),
            having(gh -> {
                    gh.setRepo("x/y");
                    return gh;
                },
                having(gh -> {
                        gh.accessToken = "abc";
                        return gh;
                    },
                    having(gh -> gh.containsIssue(new Closure<Predicate<Object>>(gh)
                    {
                        @Override
                        public Predicate<Object> call()
                        {
                            return any -> true;
                        }
                    }), having(p -> p.test("123"), is(true))))
            )
        );
    }


    @Test
    void testMismatch()
    {
        FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> mockIssueFunction =
            mock(FragileBiFunction.class,
                with(f -> f.value(eq("x/y"), any()), returning(
                    mock(FragileFunction.class,
                        with(f -> f.value(any()), throwing(new AssertionError("unexpected call"))),
                        with(f -> f.value(123), returning("{}")))
                )));

        assertThat(new GitHub(mockIssueFunction),
            having(gh -> {
                    gh.setRepo("x/y");
                    return gh;
                },
                having(gh -> {
                        gh.accessToken = "abc";
                        return gh;
                    },
                    having(gh -> gh.containsIssue(new Closure<Predicate<Object>>(gh)
                    {
                        @Override
                        public Predicate<Object> call()
                        {
                            return any -> false;
                        }
                    }), having(p -> p.test("123"), is(false))))
            )
        );
    }


    @Test
    void testNotFound()
    {
        FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> mockIssueFunction =
            mock(FragileBiFunction.class,
                with(f -> f.value(eq("x/y"), any()), returning(
                    mock(FragileFunction.class,
                        with(f -> f.value(any()), throwing(new AssertionError("unexpected call"))),
                        with(f -> f.value(123), throwing(new IOException("Not Found"))))
                )));

        assertThat(new GitHub(mockIssueFunction),
            having(gh -> {
                    gh.setRepo("x/y");
                    return gh;
                },
                having(gh -> {
                        gh.accessToken = "abc";
                        return gh;
                    },
                    having(gh -> gh.containsIssue(new Closure<Predicate<Object>>(gh)
                    {
                        @Override
                        public Predicate<Object> call()
                        {
                            return any -> true;
                        }
                    }), having(p -> p.test("123"), is(false))))
            )
        );
    }
}