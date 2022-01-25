package org.dmfs.gradle.gitversion.git;

import org.dmfs.jems2.FragileFunction;
import org.dmfs.jems2.comparator.Reverse;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Seq;
import org.dmfs.jems2.iterable.Sorted;
import org.dmfs.jems2.optional.First;
import org.dmfs.jems2.optional.FirstPresent;
import org.dmfs.jems2.optional.MapEntry;
import org.dmfs.jems2.single.Backed;
import org.dmfs.semver.*;
import org.dmfs.semver.comparators.VersionComparator;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.jgit.lib.Constants.R_TAGS;


public final class GitVersion implements FragileFunction<Repository, Version, Exception>
{
    private final ChangeTypeStrategy mStrategy;


    public GitVersion(ChangeTypeStrategy strategy)
    {
        mStrategy = strategy;
    }


    @Override
    public Version value(Repository repository) throws Exception
    {
        return readVersion(new RevWalk(repository), repository.parseCommit(repository.resolve("HEAD")), versions(repository));
    }


    private Version readVersion(RevWalk revWalk, RevCommit commit, Map<ObjectId, Version> tags)
    {
        return new Backed<>(
            new FirstPresent<>(
                new MapEntry<>(tags, commit.getId()),
                new First<>(
                    new Sorted<>(new Reverse<>(new VersionComparator()),
                        new Mapped<>(v -> mStrategy.changeType(commit, "<FIXME>")
                            .next(v, new NextPreRelease(v).preRelease(), commit.getId().getName().substring(0, 10)),
                            new Mapped<>(commit1 -> readVersion(revWalk, commit1, tags), new Seq<>(parsed(revWalk, commit).getParents())))))),
            () -> new PatchPreRelease(new Release(0, 0, 0), "alpha")).value();
    }


    private RevCommit parsed(RevWalk revWalk, RevCommit commit)
    {
        try
        {
            return revWalk.parseCommit(commit.getId());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Can't parse commit", e);
        }
    }


    private Map<ObjectId, Version> versions(Repository repository) throws GitAPIException
    {
        VersionParser parser = new StrictParser();
        Map<ObjectId, Version> result = new HashMap<>();

        for (Ref tag : new Git(repository).tagList().call())
        {
            try
            {
                String tagName = tag.getName().startsWith(R_TAGS) ? tag.getName().substring(R_TAGS.length()) : tag.getName();
                Version version = parser.parse(tagName);
                if (!result.containsKey(tag.getObjectId()) || new VersionComparator().compare(result.get(tag.getObjectId()), version) < 0)
                {
                    result.put(tag.getObjectId(), version);
                }
            }
            catch (Exception e)
            {
                // ignore non-version tags
            }
        }
        return result;
    }

}
