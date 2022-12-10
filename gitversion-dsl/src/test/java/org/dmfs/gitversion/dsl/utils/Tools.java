package org.dmfs.gitversion.dsl.utils;

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

import static org.dmfs.gitversion.dsl.utils.Matchers.given;


public final class Tools
{
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
            {
                File createdFolder = Files.createTempDirectory("testFolder").toFile();
                return createdFolder;
            },
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
