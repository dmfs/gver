package org.dmfs.gradle.gitversion.dsl.issuetracker;

import org.apache.groovy.json.internal.IO;
import org.dmfs.jems2.FragileFunction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Consumer;


final class GitHubIssueFunction implements FragileFunction<Integer, String, Exception>
{
    private final String mRepoName;
    private final Consumer<HttpURLConnection> mAuthenticator;


    GitHubIssueFunction(String repoName, Consumer<HttpURLConnection> authenticator)
    {
        mRepoName = repoName;
        mAuthenticator = authenticator;
    }


    @Override
    public String value(Integer issueNumber) throws Exception
    {
        if (!mRepoName.matches("^[\\w\\d-]+/[\\w\\d_.-]+$"))
        {
            throw new IllegalArgumentException("Illegal repo name " + mRepoName);
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(
            String.format(Locale.ENGLISH, "https://api.github.com/repos/%s/issues/%s", mRepoName, issueNumber)).openConnection();
        try (InputStream inputStream = connection.getInputStream())
        {
            mAuthenticator.accept(connection);
            StringWriter sw = new StringWriter();
            IO.copy(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), sw);
            return sw.toString();
        }
        finally
        {
            connection.disconnect();
        }
    }
}
