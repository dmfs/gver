package org.dmfs.gver.dsl.issuetracker;

import org.dmfs.gver.dsl.IssueTracker;
import org.dmfs.jems2.FragileBiFunction;
import org.dmfs.jems2.FragileFunction;

import java.util.Optional;
import java.util.function.Predicate;

import groovy.json.JsonSlurper;
import groovy.lang.Closure;


public final class Gitea implements IssueTracker
{
    private String mHost = "";
    private String mRepo = "";
    public String accessToken = "";

    private final FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> issueFunction;


    public Gitea()
    {
        this((baseUrl, accessToken) ->
            new GiteaIssueFunction(baseUrl, connection -> accessToken.ifPresent(token -> connection.setRequestProperty("Authorization", "token " + token))));
    }


    public Gitea(FragileBiFunction<String, Optional<String>, FragileFunction<Integer, String, Exception>, Exception> issueFunction)
    {
        this.issueFunction = issueFunction;
    }


    public void setHost(String host)
    {
        this.mHost = host;
    }


    public void setRepo(String repo)
    {
        if (!repo.matches("^[\\w\\d-]+/[\\w\\d_.-]+$"))
        {
            throw new IllegalArgumentException("Illegal repo name " + repo);
        }
        this.mRepo = repo;
    }


    @Override
    public Predicate<String> containsIssue(Closure<Predicate<Object>> delegate)
    {
        return issue ->
        {
            try
            {
                delegate.setDelegate(new GiteaDsl());
                if (delegate.call().test(new JsonSlurper().parseText(
                    issueFunction.value(String.format(
                        "https://%s/api/v1/repos/%s/issues", mHost, mRepo
                    ), Optional.of(accessToken).filter(s -> !s.isEmpty())).value(Integer.valueOf(issue)))))
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
