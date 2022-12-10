package org.dmfs.gitversion.git.changetypefacories;

import org.eclipse.jgit.revwalk.RevCommit;


public interface Condition
{
    boolean matches(RevCommit commit, String branch);
}
