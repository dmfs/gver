package org.dmfs.gradle.gitversion.dsl;

import java.util.function.Predicate;

import groovy.lang.Closure;


public interface IssueTracker
{
    Predicate<String> containsIssue(Closure<Boolean> matcher);
}
