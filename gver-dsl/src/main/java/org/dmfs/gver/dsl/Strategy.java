package org.dmfs.gver.dsl;

import groovy.lang.Closure;
import org.dmfs.gver.git.ChangeType;
import org.dmfs.gver.git.ChangeTypeStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;


public class Strategy
{
    public final List<ChangeTypeStrategy> mChangeTypeStrategies;
    public final static ChangeType major = ChangeType.MAJOR;
    public final static ChangeType minor = ChangeType.MINOR;
    public final static ChangeType patch = ChangeType.PATCH;
    public final static ChangeType none = ChangeType.NONE;
    public final static ChangeType invalid = ChangeType.INVALID;

    public Strategy()
    {
        this(new ChangeTypeStrategy[0]);
    }

    public Strategy(ChangeTypeStrategy... strategies)
    {
        mChangeTypeStrategies = new ArrayList<>(asList(strategies));
    }

    public ConditionConsumer are(ChangeType changeType)
    {
        return condition -> {

            Conditions conditions = new Conditions();
            condition.setResolveStrategy(Closure.DELEGATE_FIRST);
            condition.setDelegate(conditions);
            condition.call();

            mChangeTypeStrategies.add(changeType.when(conditions));
        };
    }


    public void otherwise(ChangeType changeType)
    {
        mChangeTypeStrategies.add(changeType.when(((repository, commit, branches) -> true)));
    }


    public void follow(Closure strategy)
    {
        strategy.setResolveStrategy(Closure.DELEGATE_FIRST);
        strategy.setDelegate(this);
        strategy.call();
    }


}