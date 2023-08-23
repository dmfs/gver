package org.dmfs.gver.dsl.utils;

import org.dmfs.jems2.FragileFunction;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;

import java.io.File;
import java.net.URL;


public final class Repository implements FragileFunction<File, WithResource.Resource<org.eclipse.jgit.lib.Repository>, Exception>
{
    private final URL mSource;
    private final String mBranch;


    public Repository(URL source, String branch)
    {
        mSource = source;
        mBranch = branch;
    }


    @Override
    public WithResource.Resource<org.eclipse.jgit.lib.Repository> value(File directory) throws Exception
    {
        Git.cloneRepository()
            .setURI(mSource.toString())
            .setDirectory(directory)
            .setBranch(mBranch)
            .call();
        org.eclipse.jgit.lib.Repository repository = new FileRepositoryBuilder().setWorkTree(directory).build();

        return new WithResource.Resource<org.eclipse.jgit.lib.Repository>()
        {
            @Override
            public void close()
            {
                repository.close();
            }


            @Override
            public org.eclipse.jgit.lib.Repository value()
            {
                return repository;
            }

        };
    }

}
