package org.dmfs.gitversion.dsl.issuetracker;

import org.dmfs.jems2.FragileFunction;

import java.io.*;
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
        mAuthenticator.accept(connection);
        try (InputStream inputStream = connection.getInputStream();
             Reader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             Reader reader = new BufferedReader(isr);
             StringWriter sw = new StringWriter();)
        {
            int readChars;
            char[] buffer = new char[10240];
            while ((readChars = reader.read(buffer)) >= 0)
            {
                sw.write(buffer, 0, readChars);
            }
            return sw.toString();
        }
        finally
        {
            connection.disconnect();
        }
    }
}
