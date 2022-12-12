package org.dmfs.gitversion.git;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;


public interface ChangeTypeStrategy
{
    ChangeType changeType(Repository repository, RevCommit commit, String branch);
}
