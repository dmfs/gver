package org.dmfs.gver.git.changetypefacories;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;


public interface Condition
{
    boolean matches(Repository repository, RevCommit commit, String branch);
}
