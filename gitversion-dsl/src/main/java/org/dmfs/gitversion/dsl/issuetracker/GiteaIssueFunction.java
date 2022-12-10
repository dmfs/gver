package org.dmfs.gitversion.dsl.issuetracker;

import org.dmfs.jems2.FragileFunction;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;


final class GiteaIssueFunction implements FragileFunction<Integer, String, Exception>
{
    private final String mBaseUrl;
    private final Consumer<HttpURLConnection> mAuthenticator;


    GiteaIssueFunction(String baseUrl, Consumer<HttpURLConnection> authenticator)
    {
        mBaseUrl = baseUrl;
        mAuthenticator = authenticator;
    }


    @Override
    public String value(Integer issueNumber) throws Exception
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(mBaseUrl + "/" + issueNumber).openConnection();
        mAuthenticator.accept(connection);
        try (InputStream inputStream = connection.getInputStream();
             Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
