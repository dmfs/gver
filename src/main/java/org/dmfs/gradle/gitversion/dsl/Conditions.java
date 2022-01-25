package org.dmfs.gradle.gitversion.dsl;

import org.dmfs.gradle.gitversion.git.changetypefacories.Condition;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.AllOf;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.Branch;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gradle.gitversion.git.changetypefacories.condition.CommitTitle;
import org.dmfs.gradle.gitversion.git.predicates.Contains;
import org.dmfs.gradle.gitversion.git.predicates.Matches;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
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


    Predicate<CharSequence> contains(String pattern)
    {
        return new Contains(pattern);
    }


    Predicate<CharSequence> contains(Pattern pattern)
    {
        return new Contains(pattern);
    }


    Predicate<CharSequence> matches(String pattern)
    {
        return new Matches(pattern);
    }


    Predicate<CharSequence> matches(Pattern pattern)
    {
        return new Matches(pattern);
    }


    @Override
    public boolean matches(RevCommit commit, String branch)
    {
        return new AllOf(mConditions).matches(commit, branch);
    }
}
