package org.dmfs.gitversion.dsl.dsl;

import org.dmfs.gitversion.dsl.GitVersionConfig;
import org.dmfs.gitversion.dsl.issuetracker.Gitea;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Closure;

import static org.dmfs.jems2.mockito.Mock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


class GitVersionConfigTest
{
    @Test
    void testGitea()
    {
        Closure<?> mockClosure = mock(Closure.class,
            withVoid(c -> c.setResolveStrategy(Closure.DELEGATE_FIRST), doingNothing()),
            withVoid(c -> c.setDelegate(any(Gitea.class)), doingNothing()),
            withVoid(Closure::call, returning(null)));

        assertThat(new GitVersionConfig(),
            has("contains", dsl -> dsl.Gitea(mockClosure),
                is(instanceOf(Gitea.class))));

        verify(mockClosure).setResolveStrategy(Closure.DELEGATE_FIRST);
        verify(mockClosure).setDelegate(any(Gitea.class));
        verify(mockClosure).call();
    }


    @Test
    void testContains()
    {
        Closure<Predicate<Matcher>> mockClosure = new Closure<Predicate<Matcher>>(this)
        {
            @Override
            public Predicate<Matcher> call()
            {
                return Matcher::matches;
            }
        };

        assertThat(new GitVersionConfig(),
            has("contains", dsl -> dsl.contains(Pattern.compile("123#(?<gr>\\d+)"), mockClosure),
                allOf(satisfiedBy("123#456"),
                    not(satisfiedBy("123#abc")))));
    }

}