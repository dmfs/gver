package org.dmfs.gver.git.changetypefacories.condition;

import org.dmfs.gver.git.changetypefacories.Condition;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.function.Predicate;


public final class CommitTitle implements Condition
{
    private final Predicate<? super String> mPredicate;


    public CommitTitle(Predicate<? super String> predicate)
    {
        mPredicate = predicate;
    }


    @Override
    public boolean matches(Repository repository, RevCommit commit, String branch)
    {
        return mPredicate.test(commit.getShortMessage());
    }
}
