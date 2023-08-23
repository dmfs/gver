package org.dmfs.gradle.gver.utils;

import org.dmfs.jems2.Function;
import org.dmfs.jems2.iterable.Joined;
import org.dmfs.jems2.iterable.Seq;
import org.dmfs.jems2.optional.Mapped;
import org.dmfs.jems2.optional.NullSafe;
import org.dmfs.jems2.procedure.ForEach;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matcher;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static org.dmfs.gradle.gver.utils.Matchers.given;


public final class Tools
{
    public static <T> Matcher<T> withTestProject(File projectDir, Function<Project, ? extends Matcher<? super T>> delegateFunction)
    {
        return given(() -> {
                Git.cloneRepository()
                    .setURI(Tools.class.getClassLoader().getResource("0.2.0.bundle").toString())
                    .setDirectory(projectDir)
                    .call();
                return ProjectBuilder.builder().withProjectDir(projectDir).build();
            },
            delegateFunction);
    }


    public static <T> Matcher<T> withRepository(URL src, File dest, String branch, Function<Repository, ? extends Matcher<? super T>> delegateFunction)
    {
        return given(() -> {
                Git.cloneRepository()
                    .setURI(src.toString())
                    .setDirectory(dest)
                    .setBranch(branch)
                    .call();
                return new FileRepositoryBuilder().setWorkTree(dest).build();
            },
            delegateFunction,
            Repository::close);
    }


    public static <T> Matcher<T> withTempFolder(Function<File, ? extends Matcher<? super T>> delegateFunction)
    {
        return given(() ->
                Files.createTempDirectory("testFolder").toFile(),
            delegateFunction,
            Tools::delete
        );
    }


    private static void delete(File file)
    {
        new ForEach<>(new Joined<>(new Mapped<>(Seq::new, new NullSafe<>(file.listFiles())))).process(Tools::delete);
        file.delete();
    }
}
