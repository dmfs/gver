package org.dmfs.gitversion.dsl;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import groovy.lang.Closure;


public final class TextDsl
{
    private final Optional<IssueTracker> mTracker;


    public TextDsl(Optional<IssueTracker> tracker)
    {
        mTracker = tracker;
    }


    public Predicate<Matcher> where(String group, Closure<Predicate<String>> is)
    {
        is.setResolveStrategy(Closure.DELEGATE_FIRST);
        return m -> {
            if (m.group(group) != null)
            {
                String match = m.group(group);
                is.setDelegate(new IssueDsl(mTracker));
                return is.call().test(match);
            }
            return false;
        };
    }

}
