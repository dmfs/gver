package org.dmfs.gradle.gver.utils;

import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.Suffixes;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.saynotobugs.confidence.junit5.engine.Resource;
import org.saynotobugs.confidence.junit5.engine.ResourceComposition;
import org.saynotobugs.confidence.junit5.engine.resource.LazyResource;

import java.io.File;


public final class TestProject extends ResourceComposition<Project>
{
    public TestProject(Resource<File> projectDir, Strategy strategy, Resource<File> homeDir)
    {
        this(projectDir, strategy, new Suffixes(), homeDir);
    }

    public TestProject(Resource<File> projectDir, Strategy strategy)
    {
        this(projectDir, strategy, new Suffixes(), projectDir);
    }

    public TestProject(Resource<File> projectDir, Strategy strategy, Suffixes suffixes, Resource<File> homeDir)
    {
        // TODO, this should be a Resource derived from two values
        super(new LazyResource<>(
            () -> {
                Project project = ProjectBuilder.builder()
                    .withProjectDir(projectDir.value())
                    .withGradleUserHomeDir(homeDir.value())
                    .build();
                project.getPluginManager().apply("org.dmfs.gver");
                ((GitVersionConfig) project.getExtensions().getByName("gver")).mChangeTypeStrategy = strategy;
                ((GitVersionConfig) project.getExtensions().getByName("gver")).mSuffixes = suffixes;
                return project;
            },
            project -> {
                projectDir.close();
                homeDir.close();
            }));
    }
}
