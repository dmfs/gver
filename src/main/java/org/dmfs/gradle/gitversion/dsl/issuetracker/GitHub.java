package org.dmfs.gradle.gitversion.dsl.issuetracker;

import org.dmfs.gradle.gitversion.dsl.IssueTracker;
import org.dmfs.jems2.FragileBiFunction;
import org.dmfs.jems2.FragileFunction;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.json.JsonSlurper;
import groovy.lang.Closure;


public final class GitHub implements IssueTracker
{
    private String repo = "";
    public String accessToken = "";

    private final FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> issueFunction;


    public GitHub()
    {
        this((repo, accessToken) ->
            new GitHubIssueFunction(repo, connection -> accessToken.ifPresent(token -> connection.setRequestProperty("Authorization", "token " + token))));
    }


    public GitHub(FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> issueFunction)
    {
        this.issueFunction = issueFunction;
    }


    public void setRepo(String repo)
    {
        if (!repo.matches("^[\\w\\d-]+/[\\w\\d_.-]+$"))
        {
            throw new IllegalArgumentException("Illegal repo name " + repo);
        }
        this.repo = repo;
    }


    @Override
    public Predicate<String> containsIssue(Closure<Boolean> delegate)
    {
        return commit ->
        {
            Pattern p = Pattern.compile("#(\\d+)");
            Matcher matcher = p.matcher(commit);
            if (matcher.find()) // for now just look at the first one, later this might be changed to a `while` to check all issues
            {
                try
                {
                    if (delegate.call(new JsonSlurper().parseText(
                        issueFunction.value(repo, Optional.of(accessToken).filter(s -> !s.isEmpty())).value(Integer.valueOf(matcher.group(1))))))
                    {
                        return true;
                    }
                }
                catch (Exception e)
                {
                    // ignore and move on to the next one
                }
            }
            return false;
        };
    }

}
