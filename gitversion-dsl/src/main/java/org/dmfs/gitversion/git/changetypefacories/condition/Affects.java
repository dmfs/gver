package org.dmfs.gradle.gitversion.git.changetypefacories.condition;

import org.dmfs.gitversion.git.changetypefacories.Condition;
import org.dmfs.jems2.iterable.PresentValues;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.procedure.ForEach;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public final class Affects implements Condition
{
    private final Predicate<? super Set<String>> mPredicate;


    public Affects(Predicate<? super Set<String>> predicate)
    {
        mPredicate = predicate;
    }


    @Override
    public boolean matches(Repository repository, RevCommit commit, String branch)
    {
        Set<String> files = new HashSet<>();
        try (Git git = new Git(repository);
             ObjectReader reader = repository.newObjectReader())
        {
            RevCommit[] parents = commit.getParents();

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            new ForEach<>(parents).process(parent -> {
                try
                {
                    if (parent.getTree() == null)
                    {
                        parent = repository.parseCommit(parent.getId());
                    }
                    if (parent.getTree() != null)
                    {
                        oldTreeIter.reset(reader, parent.getTree().toObjectId());
                    }
                    if (commit.getTree() != null)
                    {
                        newTreeIter.reset(reader, commit.getTree().toObjectId());
                    }
                    List<DiffEntry> changes = git.diff()
                        .setNewTree(commit.getTree() != null ? newTreeIter : new EmptyTreeIterator())
                        .setOldTree(parent.getTree() != null ? oldTreeIter : new EmptyTreeIterator())
                        .call();
                    changes.forEach(de ->
                        new ForEach<>(new PresentValues<>(new NullSafe<>(de.getNewPath()), new NullSafe<>(de.getOldPath())))
                            .process(files::add));
                }
                catch (IOException | GitAPIException e)
                {
                    throw new RuntimeException(e);
                }
            });
            return mPredicate.test(files);
        }
    }
}
