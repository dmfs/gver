package org.dmfs.gitversion.git.changetypefacories;

import org.dmfs.gitversion.git.ChangeType;
import org.dmfs.gitversion.git.ChangeTypeStrategy;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Seq;
import org.dmfs.jems2.optional.First;
import org.dmfs.jems2.single.Backed;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import static org.dmfs.gitversion.git.ChangeType.UNKNOWN;


/**
 * Returns the value of the first {@link ChangeTypeStrategy} that's not {@code UNKNOWN}.
 */
public final class FirstOf implements ChangeTypeStrategy
{
    private final Iterable<? extends ChangeTypeStrategy> mDelegates;


    public FirstOf(ChangeTypeStrategy... delegates)
    {
        this(new Seq<>(delegates));
    }


    public FirstOf(Iterable<? extends ChangeTypeStrategy> delegates)
    {
        mDelegates = delegates;
    }


    @Override
    public ChangeType changeType(Repository repository, RevCommit commit, String branch)
    {
        return new Backed<>(
            new First<>(
                type -> type != UNKNOWN,
                new Mapped<>(delegate -> delegate.changeType(repository, commit, branch), mDelegates)),
            UNKNOWN).value();
    }
}
