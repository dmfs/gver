package org.dmfs.gradle.gitversion.dsl;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.json.JsonSlurper;
import groovy.lang.Closure;


public class GitVersionConfig
{
    public Strategy mChangeTypeStrategy = new Strategy();

    public Optional<String> githubRepo = Optional.empty();

    public Pattern mReleaseBranchPattern = Pattern.compile("(main|master)$");

    public void setGithubRepo(String githubRepo)
    {
        this.githubRepo = Optional.of(githubRepo);
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


    public Predicate<String> referencesGithubIssue(Closure<Boolean> matcher)
    {
        return commit ->
        {
            Pattern p = Pattern.compile("#(\\d+)");
            Matcher matcher1 = p.matcher(commit);
            if (!matcher1.find())
            {
                return false;
            }

            try
            {
                return matcher.call(
                    new JsonSlurper()
                        .parse(new URL(String.format("https://api.github.com/repos/%s/issues/%s", githubRepo.get(), matcher1.group(1)))));
            }
            catch (IOException e)
            {
                return false;
            }
        };
    }
}