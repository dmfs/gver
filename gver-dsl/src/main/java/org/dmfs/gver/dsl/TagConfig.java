package org.dmfs.gver.dsl;

import groovy.lang.Closure;
import org.dmfs.gver.git.changetypefacories.Condition;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TagConfig
{
    public final Map<Condition, ReleaseType> mTagMappings = new LinkedHashMap<>();

    public final ReleaseType release = ReleaseType.RELEASE;
    public final ReleaseType preRelease = ReleaseType.PRERELEASE;
    public final ReleaseType none = ReleaseType.NONE;

    public ConditionConsumer with(ReleaseType type)
    {
        return new ConditionConsumer()
        {
            @Override
            public void when(Closure<?> condition)
            {
                Conditions conditions = new Conditions();
                condition.setResolveStrategy(Closure.DELEGATE_FIRST);
                condition.setDelegate(conditions);
                condition.call();

                mTagMappings.put(conditions, type);
            }

            @Override
            public Void getOtherwise()
            {
                mTagMappings.put((repository, commit, branch) -> true, type);
                return null;
            }

        };
    }
}
