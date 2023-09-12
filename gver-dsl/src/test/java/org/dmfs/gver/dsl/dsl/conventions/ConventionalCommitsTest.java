package org.dmfs.gver.dsl.dsl.conventions;

import org.dmfs.gver.dsl.ConditionConsumer;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.dsl.conventions.ConventionalCommits;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.mockito.Mock.mock;
import static org.dmfs.jems2.mockito.Mock.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.assertionThat;
import static org.saynotobugs.confidence.quality.Core.when;
import static org.saynotobugs.confidence.quality.Core.*;

@Confidence
public final class ConventionalCommitsTest
{
    Assertion convnetionalCommits_sets_right_strategy =
        assertionThat(ConventionalCommits::new,
            mutatesArgument(
                () -> mock(Strategy.class,
                    with(m -> m.are(MAJOR), returning(mock(ConditionConsumer.class, withVoid(cc -> cc.when(any()), doingNothing())))),
                    with(m -> m.are(MINOR), returning(mock(ConditionConsumer.class, withVoid(cc -> cc.when(any()), doingNothing())))),
                    with(m -> m.are(PATCH), returning(mock(ConditionConsumer.class, withVoid(cc -> cc.when(any()), doingNothing()))))),
                soIt(successfully(new Text("added changes"), new Text("failed to add changes"), (Strategy m) -> {
                    verify(m, times(2)).are(MAJOR);
                    verify(m).are(MINOR);
                    verify(m).are(PATCH);
                    verifyNoMoreInteractions(m);
                })),
                when(successfully(new Text("called Closure"), new Text("called Closure"), (ConventionalCommits convention) -> convention.call()))
            ));
}
