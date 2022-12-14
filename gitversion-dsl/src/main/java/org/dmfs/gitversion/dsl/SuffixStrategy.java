package org.dmfs.gitversion.dsl;

import org.dmfs.jems2.Optional;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;


public interface SuffixStrategy
{
    Optional<String> changeType(Repository repository, RevCommit commit, String branch);
}
