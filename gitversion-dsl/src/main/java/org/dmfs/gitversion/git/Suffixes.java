package org.dmfs.gitversion.git;

import org.dmfs.gitversion.dsl.Conditions;
import org.dmfs.gitversion.dsl.SuffixPattern;
import org.dmfs.gitversion.dsl.SuffixStrategy;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.Absent;
import org.dmfs.jems2.optional.FirstPresent;
import org.dmfs.jems2.optional.Present;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import groovy.lang.Closure;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;


public final class Suffixes
{
    public final List<SuffixStrategy> mSuffixes = new ArrayList<>(asList(new SuffixStrategy()
    {
        @Override
        public Optional<String> changeType(Repository repository, RevCommit commit, String branch)
        {
            return new Present<>("." + new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH).format(new Date()) + "-SNAPSHOT");
        }
    }));


    public SuffixPattern append(String suffix)
    {
        SuffixStrategy defaultStrategy = new SuffixStrategy()
        {
            @Override
            public Optional<String> changeType(Repository repository, RevCommit commit, String branch)
            {
                return new Present<>(suffix);
            }
        };

        mSuffixes.add(defaultStrategy);
        return
            condition -> {

                Conditions conditions = new Conditions();
                condition.setResolveStrategy(Closure.DELEGATE_FIRST);
                condition.setDelegate(conditions);
                condition.call();
                mSuffixes.set(mSuffixes.indexOf(defaultStrategy),
                    (repository, commit, branches) -> conditions.matches(repository, commit, branches) ? new Present<>(suffix) : Absent.absent());
            };
    }


    Optional<String> suffix(Repository repository, RevCommit commit, String branch)
    {
        return new org.dmfs.jems2.optional.Mapped<String, String>(
            suffix -> suffix.replace('_', '-').replaceAll("[^.a-zA-Z0-9-]", ""),
            new FirstPresent<>(
                new Mapped<>(suffixStrategy -> suffixStrategy.changeType(repository, commit, branch), mSuffixes)));
    }
}
