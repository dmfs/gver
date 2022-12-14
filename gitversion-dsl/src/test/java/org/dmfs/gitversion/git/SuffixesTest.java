package org.dmfs.gitversion.git;

import org.dmfs.gitversion.dsl.Conditions;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import groovy.lang.Closure;

import static org.dmfs.gitversion.dsl.utils.Qualities.present;
import static org.dmfs.jems2.mockito.Mock.mock;
import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;


class SuffixesTest
{
    @Test
    void testDefault()
    {
        assertThat(new Suffixes(),
            has("default", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "branchName"),
                is(present(matchesPattern(".20\\d{6}T\\d{6}Z-SNAPSHOT")))));
    }


    @Test
    void testConfigured()
    {
        assertThat(new Suffixes(),
            has("with config",
                s -> {
                    s.mSuffixes.clear();
                    s.append("somesuffix");
                    return s;
                }
                ,
                has("default", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "branchName"),
                    is(present("somesuffix")))));
    }


    @Test
    void testComplexConfigured()
    {
        assertThat(new Suffixes(),
            has("with config",
                s -> {
                    s.mSuffixes.clear();
                    s.append("somesuffix").when(
                        new Closure<Object>(s)
                        {
                            @Override
                            public Object call()
                            {
                                ((Conditions) getDelegate()).branch("branchName"::equals);
                                return null;
                            }
                        }
                    );
                    s.append("default");
                    return s;
                },
                allOf(has("suffix", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "branchName"),
                        is(present("somesuffix"))),
                    has("suffix", s -> s.suffix(mock(Repository.class), mock(RevCommit.class), "otherbranch"),
                        is(present("default"))))));
    }
}