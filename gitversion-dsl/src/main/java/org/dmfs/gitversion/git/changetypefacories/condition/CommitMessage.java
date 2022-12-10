package org.dmfs.gitversion.git.changetypefacories.condition;

import org.dmfs.gitversion.git.changetypefacories.Condition;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.function.Predicate;


public final class CommitMessage implements Condition
{
    private final Predicate<? super String> mPredicate;


    public CommitMessage(Predicate<? super String> predicate)
    {
        mPredicate = predicate;
    }


    @Override
    public boolean matches(RevCommit commit, String branch)
    {
        return mPredicate.test(commit.getFullMessage());
    }
}
