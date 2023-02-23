package org.dmfs.gver.git.predicates;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.util.function.Predicate;


public final class IsDirty implements Predicate<Repository>, org.dmfs.jems2.Predicate<Repository>
{
    @Override
    public boolean test(Repository repository)
    {
        return satisfiedBy(repository);
    }


    @Override
    public boolean satisfiedBy(Repository repository)
    {
        try
        {
            return !new Git(repository).diff().call().isEmpty() || !new Git(repository).diff().setCached(true).call().isEmpty();
        }
        catch (GitAPIException e)
        {
            throw new RuntimeException("Can't determine repository state", e);
        }
    }
}
