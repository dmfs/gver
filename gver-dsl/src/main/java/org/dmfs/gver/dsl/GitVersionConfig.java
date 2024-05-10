package org.dmfs.gver.dsl;

import groovy.lang.Closure;
import org.dmfs.gver.dsl.conventions.ConventionalCommits;
import org.dmfs.gver.dsl.conventions.StrictConventionalCommits;
import org.dmfs.gver.dsl.issuetracker.GitHub;
import org.dmfs.gver.dsl.issuetracker.Gitea;
import org.dmfs.gver.git.Suffixes;
import org.dmfs.gver.git.changetypefacories.condition.Branch;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GitVersionConfig
{
    public Strategy mChangeTypeStrategy = new Strategy();

    public Optional<IssueTracker> issueTracker = Optional.empty();

    public PreReleaseConfig mPreReleaseStrategies = new PreReleaseConfig();

    public Suffixes mSuffixes = new Suffixes();

    // TODO: find a better way to provide these, this approach doesn't scale well
    public Closure conventionalCommits = new ConventionalCommits(this);
    public Closure strictConventionalCommits = new StrictConventionalCommits(this);

    public TagConfig mTagConfig = new TagConfig();

    public GitVersionConfig()
    {
        // provide legacy config as default
        setReleaseBranchPattern(Pattern.compile("main|master"));
    }

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


    public IssueTracker Gitea(Closure<?> closure)
    {
        IssueTracker issueTracker = new Gitea();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(issueTracker);
        closure.call();
        return issueTracker;
    }


    public void setReleaseBranchPattern(Pattern releaseBranchPattern)
    {
        mTagConfig.mTagMappings.put(new Branch(branch -> releaseBranchPattern.matcher(branch).matches()), ReleaseType.RELEASE);
        mTagConfig.mTagMappings.put(new Branch(branch -> releaseBranchPattern.matcher(branch).matches()), ReleaseType.PRERELEASE);
    }


    public void changes(Closure<?> closure)
    {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(mChangeTypeStrategy);
        closure.call();
    }

    public Predicate<String> contains(Pattern pattern, Closure<Predicate<Matcher>> delegate)
    {
        delegate.setResolveStrategy(Closure.DELEGATE_FIRST);
        return s -> {
            Matcher matcher = pattern.matcher(s);
            int pos = 0;
            while (matcher.find(pos))
            {
                pos = matcher.end();
                delegate.setDelegate(new RegExDsl(issueTracker));
                if (delegate.call().test(matcher))
                {
                    return true;
                }
            }
            return false;
        };
    }


    public <T> Predicate<T> not(Predicate<T> delegate)
    {
        return delegate.negate();
    }


    public void preReleases(Closure<?> closure)
    {
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(mPreReleaseStrategies);
        closure.call();
    }


    public void suffixes(Closure<?> closure)
    {
        mSuffixes.mSuffixes.clear();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(mSuffixes);
        closure.call();
    }

    public void tag(Closure<?> closure)
    {
        mTagConfig.mTagMappings.clear();
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.setDelegate(mTagConfig);
        closure.call();
    }

}