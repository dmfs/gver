package org.dmfs.gradle.gver.utils;

import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.jems2.Fragile;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.saynotobugs.confidence.junit5.engine.assertion.WithResource;

import java.io.File;


public final class TestProject implements Fragile<WithResource.Resource<Project>, Exception>
{
    private final File mProjectDir;
    private final Strategy mStrategy;

    public TestProject(File projectDir, Strategy strategy)
    {
        mProjectDir = projectDir;
        mStrategy = strategy;
    }


    @Override
    public WithResource.Resource<Project> value() throws Exception
    {
        Project project = ProjectBuilder.builder().withProjectDir(mProjectDir).build();
        project.getPluginManager().apply("org.dmfs.gver");
        ((GitVersionConfig) project.getExtensions().getByName("gver")).mChangeTypeStrategy = mStrategy;

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
