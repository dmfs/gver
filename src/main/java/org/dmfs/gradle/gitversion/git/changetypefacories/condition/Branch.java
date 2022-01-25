package org.dmfs.gradle.gitversion.git.changetypefacories.condition;

import org.dmfs.gradle.gitversion.git.changetypefacories.Condition;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.function.Predicate;


public final class Branch implements Condition
{
    private final Predicate<? super String> mPredicate;


    public Branch(Predicate<? super String> predicate)
    {
        mPredicate = predicate;
    }


    @Override
    public boolean matches(RevCommit commit, String branch)
    {
        return mPredicate.test(branch);
    }
}
