package org.dmfs.gver.dsl;

import java.util.function.Predicate;

import groovy.lang.Closure;


public interface IssueTracker
{
    Predicate<String> containsIssue(Closure<Predicate<Object>> matcher);
}
