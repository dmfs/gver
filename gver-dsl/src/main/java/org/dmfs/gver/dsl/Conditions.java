package org.dmfs.gver.dsl;

import org.dmfs.gver.git.changetypefacories.Condition;
import org.dmfs.gver.git.changetypefacories.condition.*;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.gver.git.predicates.Matches;
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


    public void commitTitle(Predicate<? super String> predicate)
    {
        mConditions.add(new CommitTitle(predicate));
    }


    public void commitMessage(Predicate<? super String> predicate)
    {
        mConditions.add(new CommitMessage(predicate));
    }


    public void branch(Predicate<? super String> predicate)
    {
        mConditions.add(new Branch(predicate));
    }


    public void affects(Predicate<? super Set<String>> predicate)
    {
        mConditions.add(new Affects(predicate));
    }


    public void envVariable(String variableName, Predicate<? super String> predicate)
    {
        mConditions.add(new EnvVariable(variableName, predicate));
    }

    @SafeVarargs
    public static Predicate<Set<String>> anyThat(Predicate<String>... delegates)
    {
        return set -> Arrays.stream(delegates).anyMatch(stringPredicate -> set.stream().anyMatch(stringPredicate));
    }


    @SafeVarargs
    public static Predicate<Set<String>> noneThat(Predicate<String>... delegates)
    {
        return set -> Arrays.stream(delegates).noneMatch(stringPredicate -> set.stream().anyMatch(stringPredicate));
    }


    @SafeVarargs
    public static Predicate<Set<String>> only(Predicate<String>... delegates)
    {
        return set -> set.stream().allMatch(string -> Arrays.stream(delegates).anyMatch(stringPredicate -> stringPredicate.test(string)));
    }


    @Deprecated
    public static Predicate<CharSequence> contains(String pattern)
    {
        return new Contains(pattern);
    }


    public static Predicate<CharSequence> contains(Pattern pattern)
    {
        return new Contains(pattern);
    }


    @Deprecated
    public static Predicate<CharSequence> matches(String pattern)
    {
        return new Matches(pattern);
    }


    public static Predicate<CharSequence> matches(Pattern pattern)
    {
        return new Matches(pattern);
    }


    @Override
    public boolean matches(Repository repository, RevCommit commit, String branch)
    {
        return new AllOf(mConditions).matches(repository, commit, branch);
    }
}
