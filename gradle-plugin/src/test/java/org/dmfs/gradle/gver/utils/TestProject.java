package org.dmfs.gradle.gver.utils;

import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.Suffixes;
import org.dmfs.jems2.Fragile;
import org.dmfs.jems2.FragileFunction;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;

import java.io.File;


public final class TestProject implements Fragile<WithResource.Resource<Project>, Exception>,
    FragileFunction<File, WithResource.Resource<Project>, Exception>
{
    private final File mProjectDir;
    private final Strategy mStrategy;
    private final Suffixes mSuffixes;

    public TestProject(File projectDir, Strategy strategy)
    {
        this(projectDir, strategy, new Suffixes());
    }

    public TestProject(File projectDir, Strategy strategy, Suffixes suffixes)
    {
        mProjectDir = projectDir;
        mStrategy = strategy;
        mSuffixes = suffixes;
    }


    @Override
    public WithResource.Resource<Project> value() throws Exception
    {
        return value(mProjectDir);
    }

    @Override
    public WithResource.Resource<Project> value(File file) throws Exception
    {
        Project project = ProjectBuilder.builder()
            .withProjectDir(mProjectDir)
            .withGradleUserHomeDir(file)
            .build();
        project.getPluginManager().apply("org.dmfs.gver");
        ((GitVersionConfig) project.getExtensions().getByName("gver")).mChangeTypeStrategy = mStrategy;
        ((GitVersionConfig) project.getExtensions().getByName("gver")).mSuffixes = mSuffixes;


        return new WithResource.Resource<Project>()
        {
            @Override
            public void close()
            {
                /* nothing to do */
            }


            @Override
            public Project value()
            {
                return project;
            }

        };
    }
}
