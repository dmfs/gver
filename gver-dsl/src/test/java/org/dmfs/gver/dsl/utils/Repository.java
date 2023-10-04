package org.dmfs.gver.dsl.utils;

import org.dmfs.jems2.Fragile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;

import java.io.File;
import java.net.URL;


public final class Repository implements Fragile<WithResource.Resource<org.eclipse.jgit.lib.Repository>, Exception>
{
    private final URL mSource;
    private final String mBranch;
    private final File mDir;


    public Repository(URL source, String branch, File dir)
    {
        mSource = source;
        mBranch = branch;
        mDir = dir;
    }


    @Override
    public WithResource.Resource<org.eclipse.jgit.lib.Repository> value() throws Exception
    {
        Git.cloneRepository()
            .setURI(mSource.toString())
            .setDirectory(mDir)
            .setBranch(mBranch)
            .call();
        org.eclipse.jgit.lib.Repository repository = new FileRepositoryBuilder().setWorkTree(mDir).build();

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
