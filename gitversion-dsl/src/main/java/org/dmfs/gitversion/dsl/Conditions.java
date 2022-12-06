package org.dmfs.gitversion.dsl;

import org.dmfs.gitversion.git.changetypefacories.Condition;
import org.dmfs.gitversion.git.changetypefacories.condition.AllOf;
import org.dmfs.gitversion.git.changetypefacories.condition.Branch;
import org.dmfs.gitversion.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gitversion.git.changetypefacories.condition.CommitTitle;
import org.dmfs.gitversion.git.predicates.Contains;
import org.dmfs.gitversion.git.predicates.Matches;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.Affects;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;


public class Conditions implements Condition
{
    private final List<Condition> mConditions = new ArrayList<>();


    void commitTitle(Predicate<? super String> predicate)
    {
        mConditions.add(new CommitTitle(predicate));
    }


    void commitMessage(Predicate<? super String> predicate)
    {
        mConditions.add(new CommitMessage(predicate));
    }


    void branch(Predicate<? super String> predicate)
    {
        mConditions.add(new Branch(predicate));
    }


    void affects(Predicate<? super Set<String>> predicate)
    {
        mConditions.add(new Affects(predicate));
    }


    @SafeVarargs
    static Predicate<Set<String>> anyThat(Predicate<String>... delegates)
    {
        return set -> Arrays.stream(delegates).anyMatch(stringPredicate -> set.stream().anyMatch(stringPredicate));
    }


    @SafeVarargs
    static Predicate<Set<String>> noneThat(Predicate<String>... delegates)
    {
        return set -> Arrays.stream(delegates).noneMatch(stringPredicate -> set.stream().anyMatch(stringPredicate));
    }


    @SafeVarargs
    static Predicate<Set<String>> only(Predicate<String>... delegates)
    {
        return set -> set.stream().allMatch(string -> Arrays.stream(delegates).anyMatch(stringPredicate -> stringPredicate.test(string)));
    }


    @Deprecated
    static Predicate<CharSequence> contains(String pattern)
    {
        return new Contains(pattern);
    }


    static Predicate<CharSequence> contains(Pattern pattern)
    {
        return new Contains(pattern);
    }


    @Deprecated
    static Predicate<CharSequence> matches(String pattern)
    {
        return new Matches(pattern);
    }


    static Predicate<CharSequence> matches(Pattern pattern)
    {
        return new Matches(pattern);
    }


    @Override
    public boolean matches(Repository repository, RevCommit commit, String branch)
    {
        return new AllOf(mConditions).matches(repository, commit, branch);
    }
}
