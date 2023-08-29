package org.dmfs.gver.dsl;

import groovy.lang.Closure;
import org.dmfs.gver.git.ChangeType;
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
                (Strategy strategy) -> strategy.follow(new Closure(strategy)
                {
                    @Override
                    public Object call()
                    {
                        ((Strategy) getDelegate()).are(ChangeType.MINOR).when(new Closure(strategy)
                        {
                            @Override
                            public Object call(Object... args)
                            {
                                return null;
                            }
                        });
                        ((Strategy) getDelegate()).are(ChangeType.MAJOR).when(new Closure(strategy)
                        {
                            @Override
                            public Object call(Object... args)
                            {
                                return null;
                            }
                        });
                        return null;
                    }
                }),
                soIt(has("ChangeTypeStrategies", (Strategy strategy) -> strategy.mChangeTypeStrategies, iterates(
                    anything(),
                    anything()
                )))))
        );
    }

}