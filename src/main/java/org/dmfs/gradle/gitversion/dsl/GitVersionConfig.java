package org.dmfs.gradle.gitversion.dsl;

import org.dmfs.gradle.gitversion.dsl.issuetracker.GitHub;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import groovy.lang.Closure;


public class GitVersionConfig
{
    public Strategy mChangeTypeStrategy = new Strategy();

    public Optional<IssueTracker> issueTracker = Optional.empty();

    public Pattern mReleaseBranchPattern = Pattern.compile("(main|master)$");

    public PreReleaseConfig mPreReleaseStrategies = new PreReleaseConfig();


    public void setIssueTracker(IssueTracker issueTracker)
    {
        this.issueTracker = Optional.of(issueTracker);
    }


    public IssueTracker GitHub(Closure<?> closure)
    {
        IssueTracker issueTracker = new GitHub();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(issueTracker);
        closure.call();
        return issueTracker;
    }


    public void setReleaseBranchPattern(Pattern releaseBranchPattern)
    {
        mReleaseBranchPattern = releaseBranchPattern;
    }


    public void changes(Closure<?> closure)
    {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(mChangeTypeStrategy);
        closure.call();
    }


    public Predicate<String> containsIssue(Closure<Boolean> matcher)
    {
        return issueTracker.map(s -> s.containsIssue(matcher)).orElseThrow(() -> new NoSuchElementException("No issue tracker configured"));
    }


    public void preReleases(Closure<?> closure)
    {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(mPreReleaseStrategies);
        closure.call();
    }
}