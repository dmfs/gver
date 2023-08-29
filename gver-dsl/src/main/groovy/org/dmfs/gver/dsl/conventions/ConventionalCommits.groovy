package org.dmfs.gver.dsl.conventions

/**
 * Provides the Conventional Commits defaults. Only types {@code fix} and {@code feat} are interpreted all other types
 * fall through to the next change type.
 *
 * Note, if none of the rules match, this falls though to the default rule,
 * which is to increment the pre-release. If you don't want to change the version you need to append
 *
 * <pre>
 *     otherwise none
 * </pre>
 */
class ConventionalCommits extends Closure<Object> {

    ConventionalCommits(Object owner) {
        super(owner)
    }

    Object doCall() {
        are major when {
            commitMessage contains(~/(?m)^BREAKING[- ]CHANGE: /)
        }
        are major when {
            commitTitle matches(~/^[a-z]+(\([^)]+\))?!: .*/)
        }
        are minor when {
            commitTitle matches(~/^feat(\([^)]+\))?: .*/)
        }
        are patch when {
            commitTitle matches(~/^fix(\([^)]+\))?: .*/)
        }
        // fall through to default
    }
}