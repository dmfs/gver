package org.dmfs.gver.git;

import org.dmfs.gver.dsl.Conditions;
import org.dmfs.gver.dsl.SuffixPattern;
import org.dmfs.gver.dsl.SuffixStrategy;
import org.dmfs.gver.git.predicates.IsDirty;
import org.dmfs.jems2.Optional;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.optional.Absent;
import org.dmfs.jems2.optional.FirstPresent;
import org.dmfs.jems2.optional.Present;
import org.dmfs.rfc5545.DateTime;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import groovy.lang.Closure;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;


public final class Suffixes
{
    private final static Supplier<SuffixStrategy> DEFAULT_STRATEGY = () ->
        (SuffixStrategy) (repository, commit, branch) -> new Present<>("." +
            (new IsDirty().satisfiedBy(repository)
                ? DateTime.now()
                : new DateTime(commit.getCommitTime() * 1000L)) // clean repo gets the last commit date
            + "-SNAPSHOT");

    public final List<SuffixStrategy> mSuffixes = new ArrayList<>(asList(DEFAULT_STRATEGY.get()));

    /**
     * {@code "\u0000"} is not a valid suffix character. We use it to represent the default suffix.
     */
    public final static String DEFAULT = "\u0000";


    public SuffixPattern append(String suffix)
    {
        SuffixStrategy defaultStrategy = DEFAULT.equals(suffix)
            ? DEFAULT_STRATEGY.get()
            : (repository, commit, branch) -> new Present<>(suffix);

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
