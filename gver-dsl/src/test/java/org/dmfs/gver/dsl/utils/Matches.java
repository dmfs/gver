package org.dmfs.gver.dsl.utils;

import org.dmfs.gver.git.changetypefacories.Condition;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.saynotobugs.confidence.description.Spaced;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.description.ToStringDescription;
import org.saynotobugs.confidence.quality.composite.QualityComposition;
import org.saynotobugs.confidence.quality.object.Satisfies;


public final class Matches extends QualityComposition<Condition>
{
    public Matches(Repository repository, RevCommit commit, String branch)
    {
        super(
            new Satisfies<>(condition -> condition.matches(repository, commit, branch),
                new Spaced(new Text("matches commit"), new ToStringDescription(commit), new Text("and branch"),
                    new Text(branch))
            ));
    }
}