package org.dmfs.gradle.gitversion.utils;

import org.dmfs.jems2.FragileFunction;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Project;

import java.io.IOException;


/**
 * A {@link FragileFunction} that returns the {@link Repository} of a given {@link Project}.
 */
public final class ProjectRepositoryFunction implements FragileFunction<Project, Repository, IOException>
{
    public final static ProjectRepositoryFunction INSTANCE = new ProjectRepositoryFunction();


    @Override
    public Repository value(Project project) throws IOException
    {
        return new FileRepositoryBuilder().setWorkTree(project.getProjectDir()).build();
    }
}
