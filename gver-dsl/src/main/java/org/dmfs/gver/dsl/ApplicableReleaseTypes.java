package org.dmfs.gver.dsl;

import org.dmfs.jems2.iterable.DelegatingIterable;
import org.dmfs.jems2.iterable.Mapped;
import org.dmfs.jems2.iterable.Sieved;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Map;

public final class ApplicableReleaseTypes extends DelegatingIterable<ReleaseType>
{
    public ApplicableReleaseTypes(GitVersionConfig config, Repository repository, RevCommit revCommit, String name)
    {
        super(new Mapped<>(Map.Entry::getValue,
            new Sieved<>(entry -> entry.getKey().matches(repository, revCommit, name),
                config.mTagConfig.mTagMappings.entrySet())));
    }
}
