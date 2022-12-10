package org.dmfs.gitversion.git;

import org.eclipse.jgit.revwalk.RevCommit;


public interface ChangeTypeStrategy
{
    ChangeType changeType(RevCommit commit, String branch);
}
