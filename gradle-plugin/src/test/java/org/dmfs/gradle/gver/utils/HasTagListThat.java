package org.dmfs.gradle.gver.utils;

import org.dmfs.jems2.iterable.Mapped;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.saynotobugs.confidence.Quality;
import org.saynotobugs.confidence.quality.composite.QualityComposition;

import static org.saynotobugs.confidence.quality.Core.has;

public final class HasTagListThat extends QualityComposition<Repository>
{
    public HasTagListThat(Quality<? super Iterable<String>> tagQuality)
    {
        super(has("tag list that", repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()), tagQuality));
    }
}
