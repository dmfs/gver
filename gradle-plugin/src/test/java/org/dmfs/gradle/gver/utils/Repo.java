package org.dmfs.gradle.gver.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.ResourceComposition;
import org.saynotobugs.confidence.junit5.engine.resource.Derived;

import java.io.File;
import java.net.URL;


public final class Repo extends ResourceComposition<Repository>
{
    public Repo(String resourceName, String branch, Resource<File> tempDir)
    {
        this(Repo.class.getClassLoader().getResource(resourceName), branch, tempDir);
    }

    public Repo(URL source, String branch, Resource<File> tempDir)
    {
        super(new Derived<>(
            directory -> {
                Git.cloneRepository()
                    .setURI(source.toString())
                    .setDirectory(directory)
                    .setBranch(branch)
                    .call();
                return new FileRepositoryBuilder().setWorkTree(directory).build();
            },
            tempDir,
            Repository::close));
    }
}
