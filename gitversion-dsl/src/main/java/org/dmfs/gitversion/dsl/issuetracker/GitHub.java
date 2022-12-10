package org.dmfs.gitversion.dsl.issuetracker;

import org.dmfs.gitversion.dsl.IssueTracker;
import org.dmfs.jems2.FragileBiFunction;
import org.dmfs.jems2.FragileFunction;

import java.util.Optional;
import java.util.function.Predicate;

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
    public Predicate<String> containsIssue(Closure<Predicate<Object>> delegate)
    {
        return issue ->
        {
            try
            {
                delegate.setDelegate(new GitHubDsl());
                if (delegate.call().test(new JsonSlurper().parseText(
                    issueFunction.value(repo, Optional.of(accessToken).filter(s -> !s.isEmpty())).value(Integer.valueOf(issue)))))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return false;
        };
    }
}
