package org.dmfs.gradle.gitversion.dsl;

import org.dmfs.gradle.gitversion.git.ChangeType;
import org.dmfs.gradle.gitversion.git.ChangeTypeStrategy;

import java.util.ArrayList;
import java.util.List;

import groovy.lang.Closure;

import static org.dmfs.gradle.gitversion.git.ChangeType.*;


public class Strategy
{
    public final List<ChangeTypeStrategy> mChangeTypeStrategies = new ArrayList<>();
    public final static ChangeType major = MAJOR;
    public final static ChangeType minor = MINOR;
    public final static ChangeType patch = PATCH;


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
        mChangeTypeStrategies.add(changeType.when(((commit, branches) -> true)));
    }

}