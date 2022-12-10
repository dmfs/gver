package org.dmfs.gitversion.dsl.utils;

import org.dmfs.gitversion.git.changetypefacories.Condition;
import org.dmfs.srcless.annotations.staticfactory.StaticFactories;
import org.eclipse.jgit.revwalk.RevCommit;
import org.saynotobugs.confidence.description.Delimited;
import org.saynotobugs.confidence.description.TextDescription;
import org.saynotobugs.confidence.description.ToStringDescription;
import org.saynotobugs.confidence.description.ValueDescription;
import org.saynotobugs.confidence.quality.composite.QualityComposition;
import org.saynotobugs.confidence.quality.object.Satisfies;


@StaticFactories("Qualities")
public final class Matches extends QualityComposition<Condition>
{
    public Matches(RevCommit commit, String branch)
    {
        super(
            new Satisfies<>(condition -> condition.matches(commit, branch),
                new Delimited(new TextDescription("matches commit"), new ToStringDescription(commit), new TextDescription("and branch"),
                    new ValueDescription(branch))
            ));
    }
}