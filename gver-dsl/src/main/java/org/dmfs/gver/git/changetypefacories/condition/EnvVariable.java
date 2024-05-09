package org.dmfs.gver.git.changetypefacories.condition;

import org.dmfs.gver.git.changetypefacories.Condition;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.function.Predicate;


public final class EnvVariable implements Condition
{
    private final String mVariableName;
    private final Predicate<? super String> mPredicate;


    public EnvVariable(String variableName, Predicate<? super String> predicate)
    {
        mVariableName = variableName;
        mPredicate = predicate;
    }

    @Override
    public boolean matches(Repository repository, RevCommit commit, String branch)
    {
        return mPredicate.test(System.getenv().getOrDefault(mVariableName, ""));
    }
}
