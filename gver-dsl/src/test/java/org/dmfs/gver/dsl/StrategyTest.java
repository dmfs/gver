package org.dmfs.gver.dsl;

import org.dmfs.gver.dsl.utils.LambdaClosure;
import org.dmfs.gver.git.ChangeType;
import org.dmfs.jems2.Procedure;
import org.junit.jupiter.api.Test;
import org.saynotobugs.confidence.description.Text;

import static org.saynotobugs.confidence.Assertion.assertThat;
import static org.saynotobugs.confidence.quality.Core.*;

class StrategyTest
{
    @Test
    void test()
    {
        assertThat(Strategy::new,
            is(mutatedBy(
                new Text("following Closure"),
                (Strategy strategy) -> strategy.follow(new LambdaClosure<>(strategy,
                    (Procedure<Strategy>) s -> {
                        s.are(ChangeType.MINOR).when(new LambdaClosure<>(strategy, any -> null));
                        s.are(ChangeType.MAJOR).when(new LambdaClosure<>(strategy, any -> null));
                    })),
                soIt(has("ChangeTypeStrategies", (Strategy strategy) -> strategy.mChangeTypeStrategies, iterates(
                    anything(),
                    anything()
                )))))
        );
    }

}