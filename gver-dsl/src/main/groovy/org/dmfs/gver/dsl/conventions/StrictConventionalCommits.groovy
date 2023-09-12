package org.dmfs.gver.dsl.conventions

/**
 * Provides strict Conventional Commits defaults. Only types {@code fix} and {@code feat} are interpreted all other types
 * fall through to the next change type. This will cause the build to fail if the first line doesn't match the
 * Conventional Commit pattern:
 *
 * <pre>
 * &lt;type&gt;[optional scope]: &lt;description&gt;
 * </pre>
 *
 *  Note, if none of the rules match, this falls though to the default rule,
 * which is to increment the pre-release. If you don't want to change the version you need to append
 *
 * <pre>
 *     otherwise none
 * </pre>
 */
class StrictConventionalCommits extends Closure<Object> {

    StrictConventionalCommits(Object owner) {
        super(owner)
    }

    Object doCall() {
        are invalid when {
            commitTitle not(matches(~/^[a-z]+(\([^)]+\))?!?: .*/))
        }

        follow conventionalCommits

        // fall through to default
    }
}