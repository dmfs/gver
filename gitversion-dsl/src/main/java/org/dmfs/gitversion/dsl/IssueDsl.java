package org.dmfs.gitversion.dsl;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

import groovy.lang.Closure;


public final class IssueDsl
{
    private final Optional<IssueTracker> mTracker;


    public IssueDsl(Optional<IssueTracker> tracker)
    {
        mTracker = tracker;
    }


    public Predicate<String> isIssue(Closure<Predicate<Object>> delegate)
    {
        return mTracker.map(s -> s.containsIssue(delegate)).orElseThrow(() -> new NoSuchElementException("No issue tracker configured"));
    }

}
