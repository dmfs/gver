[![Build](https://github.com/dmfs/gver/actions/workflows/main.yml/badge.svg?label=main)](https://github.com/dmfs/gver/actions/workflows/main.yml)  
[![codecov](https://codecov.io/gh/dmfs/gver/branch/main/graph/badge.svg)](https://codecov.io/gh/dmfs/gver)  
[![Confidence](https://img.shields.io/badge/Tested_with-Confidence-800000?labelColor=white)](https://saynotobugs.org/confidence)


# gver

A plugin to take care of versioning your code, so you don't have to.

gver provides a domain specific language (DSL) that lets you describe in simple terms how to derive your version numbers from your
git history. This includes expressions to test the names of affected files and to check the type of referenced issues, allowing you to
skip a version update when no code or build file was altered or to derive the type of change (feature vs. bugfix) from the implemented/fixed ticket.

# Example

In order to use the plugin it needs to be configured. Currently, there is no default configuration.

The following example considers commits to be breaking changes when the commit message contains either the hashtags `#major` or `#break`. If the commit message
contains neither, the commit message is searched for the pattern `#\d` and, in case one is found, a matching issue is looked up in the Github repository
"dmfs/jems". If an issue is found, the change type is determined by whether the issue has a `bug` tag or not. If no Github issue was found, the change type is
considered to be a minor change when the commit message contains either the pattern `#feature\b` or
`(?i)\b(implement(s|ed)?|close[sd]?) #\d+\b`, and a patch when it contains `(?i)\b(fix(e[sd])?|resolve[sd]?) #\d+\b`.

```groovy
gver {
    issueTracker GitHub {
        repo = "dmfs/jems" // the name of the GitHub repo that contains the issues for this project
        if (project.hasProperty("GITHUB_API_TOKEN")) {
            accessToken = GITHUB_API_TOKEN // should be stored in your global or local gradle properties
        }
    }
    changes {
        are none when {
            // trivial changes don't get a new version number
            commitMessage contains("(?i)#trivial\\b")
        }
        are major when {
            commitMessage contains("(?i)#major\\b")
        }
        are major when {
            commitMessage contains("(?i)#break\\b")
        }
        are minor when {
            commitMessage contains(~/(?i)\b(implement(s|ed)?|close[sd]?) #(?<issue>\d+)\b/) {
                where("issue") { isIssue { not(labeled "bug") } }
            }
        }
        are patch when {
            commitMessage contains(~/(?i)\b(fix(e[sd])?|resolve[sd]?) #(?<issue>\d+)\b/) {
                where("issue") { isIssue { labeled "bug" } }
            }
        }
        otherwise patch // every other change always results in a new patch version
    }
    preReleases {
        // pre-release on the main branch are versioned like a.b.c-beta.x
        on ~/main/ use { "beta" }

        // pre-release on the feature branches are versioned like a.b.c-alpha-<feature-name>.x
        on ~/feature\/(?<name>.*)/ use { "alpha-${group('name')}" }

        // every other pre-release is versioned like a.b.c-SNAPSHOT.x
        on ~/.*/ use { "SHAPSHOT" }
    }
    suffixes {
        append ".${new Date().format("yyyyMMdd'T'HHmmss")}-SNAPSHOT" when {
            branch not(matches(~/main/))
        }
    }
    // releases can only be made on the main branch, on other branches the `gitRelease` task will fail
    releaseBranchPattern ~/main$/ // defaults to ~/(main|master)$/
}
```

## DSL

The following sections describe the DSL to specify your versioning scheme. Note that the DSL is not stable yet and may change with every new
version.

### describing change types

When practising semantic versioning, the most important step is to understand the kind of change (major, minor, bugfix). gver provides
a DSL to describe how to derive the kind of change based on commit message or referenced issues.

The top level element is `changes` which takes a closure describing when a change is considered a major, minor or bugfix change.
The list of conditions is evaluated top to bottom until the first one matches.

```groovy
gver {
    changes {
        are major when {
            // condition
        }
        are minor when {
            // another condition
        }
        // more change types
    }
}
```

A change type can appear multiple times in the list if it's to be applied under multiple conditions.

In addition to `major`, `minor`, `patch`, gver also knows a `none` type to identify trivial changes that should not result in a new version,
e.g. typo fixes in documentation files.

The `invalid` change type can be used to validate commits. As soon as the project version is determined this will
throw an exception if the condition is fulfilled and the build will fail.

Example:

```groovy
gver {
    changes {
        are invalid when {
            // enforce messages compliant to conventionalcommits.org
            commitTitle not(matches(~/^[a-z]+(\([^)]+\))?!?: .*/))
        }
    }
}
```

Note that all conditions inside a change type closure must match in order to apply the change type. If you need to
express a logical `or` just
add describe the same change type with the other condition underneath the first one.

The `otherwise` statement should be last in the list as it catches all cases that didn't match any of the other conditions. The default is to
increase any pre-release version or create a new minor pre-release if no pre-release version is present yet.

### using conventions

There are a couple of existing conventions for semantic commit messages.
At present, gver supports applying [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) using the
`follow` expression like so

```groovy
gver {
    changes {
        follow conventionalCommits
    }
}
```

You can still add rules with higher or lower priority, for instance:

```groovy
gver {
    changes {
        are major when { commitTitle contains(~/💥/) }
        are minor when { commitTitle contains(~/🎀/) }
        are patch when { commitTitle contains(~/🩹/) }
        follow conventionalCommits
        otherwise none
    }
}
```

There are two flavours `strictConventionalCommits` and `conventionalCommits`. The latter falls though and applies
the next or default change rules if a commit doesn't conform to conventional commits, whereas the former will fail and
break the build when a commit doesn't conform to the convention.

### conditions

At present, the type of change can be determined from the current branch name or the commit history up to the last tagged version.

#### `commitMessage`

`commitMessage` tests the entire commit message. You'd typically use it with `contains` or `matches`  to test it with a regular expression.

```groovy
gver {
    changes {
        are major when {
            commitMessage contains(~/#breaking\b/)
        }
    }
}
```

This identifies major changes by the presence of the `#breaking` hashtag in the commit message.

aAnother common pattern is to consider a change a bugfix when it contains one of "fixes", "fixed", "resolves" or "resolved" followed by a `#` and
a numeric issue identifier.

```groovy
gver {
    changes {
        are patch when {
            commitMessage contains("(?i)\\b(fix(e[sd])?|resolve[sd]?) #\\d+\\b")
        }
    }
}
```

#### `commitTitle`

This works almost like `commitMessage` but only takes the first line of a commit message into account.

#### `branch`

This can be used to match the name of the current head.

The following configuration considers all changes on the main branch as minor changes, whereas changes on `release` branches are considered to be
patches.

```groovy
gver {
    changes {
        are minor when {
            branch matches(~/main/)
        }
        are patch when {
            branch matches(~/release\/.*/)
        }
    }
}
```

#### affects

This condition allows you to determine a change type based on the files that have been affected by a commit. It takes a Predicate of a `Set<String>`
like `anyThat`, `noneThat` or `only`.

Example:

```groovy
gver {
    changes {
        are none when {
            affects only(matches(~/.*\.(md|adoc)/)) // only documentation updated, don't generate new version
        }
        ...
    }
}
```

#### envVariable

In some environments, e.g. Jenkins builds, the plugin may not be able to
determine branch names from the git repo.
Instead, you may have to grab the current branch name from
an environment variable like `BRANCH_NAME`. Typically, you probably
still want to combine that with the `branch` condition to support
local builds.

Example:

```groovy
gver {
    changes {
        are minor when {
            branch matches(~/feature\/.*/) // for local builds
        }
        are minor when {
            envVariable "BRANCH_NAME", matches(~/feature\/.*/) // for Jenkins builds
        }
        are patch when {
            branch matches(~/bugfix\/.*/)
        }
        are patch when {
            envVariable "BRANCH_NAME", matches(~/bugfix\/.*/)
        }
        ...
    }
}
```

### Pre-Releases

gver can apply different pre-release versions, based on the current head's name, e.g.

```groovy
gver {
    ...
    preReleases {
        on ~/main/ use { "beta" }
        on ~/feature\/(?<name>.*)/ use { "alpha-${group('name')}" }
        on ~/.*/ use { "SHAPSHOT" }
    }
}
```

In the closure passed to `use` you can use any groups declared in your regular expression. The resulting pre-release version will automatically
be sanitized to comply with semver syntax.

When your pre-release doesn't end with a numeric segment, the next pre-relase will automatically append `.1` and continue counting from that.
If the pre-release already ends with a numeric segment, it will be incremented by 1 with every subsequent pre-release.

### Suffixes

gver can append a suffix to pre-release versions. The suffix is always appended verbatim. This is primarily useful to create
`SNAPSHOT` releases.
Suffixes are specified with `append "<suffix>" when {}` where the closure contains one of the conditions also used in the `changes` DSL.

Examples

```groovy
gver {
    ...
    suffixes {
        append ".${new Date().format("yyyy-MM-dd")}-SNAPSHOT" when {
            branch not(matches(~/main/))
        }
    }
    ...
}
```

Appends a suffix like `2022-12-13-SNAPSHOT` to every pre-release that's not on a main branch.

You can set a suffix unconditionally by omitting `when` and the closure:

```groovy
gver {
    ...
    suffixes {
        append ".${new Date().format("yyyy-MM-dd")}-SNAPSHOT"
    }
    ...
}
```

Unconditional suffixes always match, hence any `append` clause following an unconditional suffix will never be applied.

If no suffixes are specified, the suffix `".<timestamp>-SNAPSHOT"` is added to every pre-release where `<timestamp>` is either the date and time
(in UTC and RFC 5545 notation) of the HEAD commit in case the working tree is clean or the current date and time if the working tree is dirty.

In order to apply the default suffix conditionally you can use the special suffix `DEFAULT` like

```groovy
gver {
    ...
    suffixes {
        append DEFAULT when { branch not(matches(~/main/)) }
    }
    ...
}
```

To disable any suffix use

```groovy
gver {
    ...
    suffixes {
        append ""
    }
    ...
}
```

### Tagging releases

gver can tag your current head with the current version or the next release version.

The task `gitTag` creates a tag with the current pre-release version. The task `gitTagRelease` creates a tag with the next release version (unless
the current commit already is a release version)

To prevent accidental release version tags on non-release branches you can provide a pattern matching your release branch names.

```groovy
gver {
    ...
    releaseBranchPattern ~/(main|release\/.*)$/
}
```

This will cause the `gitTagRelease` task to fail on any branch not matching that pattern. The default is `~/(main|master)$/`

## Issue trackers

gver can determine the type of change by checking the issues referred to in the commit message. At present, it supports two issue trackers
GitHub and Gitea.

### GitHub

If your tickets are tracked at GitHub you can determine the type of change from the labels of an issue.
First you configure gver to check issues on GitHub:

```groovy
gver {
    issueTracker GitHub {
        repo = "dmfs/gver"  // account/repo
        if (project.hasProperty("GITHUB_API_TOKEN")) { // put the api token into your global gradle properties, never under version control
            accessToken = GITHUB_API_TOKEN
        }
    }
    ...
}
```

For public repos you can omit the API token. Note, however, that GitHub has a quota for unauthenticated API requests. When you don't
provide an API token, the resulting version may be incorrect. Make sure you always provide a valid token in deployment environments.

Now you can specify change types testing the issues.

```groovy
gver {
    ...
    are minor when {
        commitMessage contains(~/#(?<issue>\d+)\b/) {
            where("issue") { isIssue { labeled "enhancement" } }
        }
    }
    are patch when {
        commitMessage contains(~/#(?<issue>\d+)\b/) {
            where("issue") { isIssue { not(labeled "enhancement") } }
        }
    }
    ...
}
```

This considers a change to be minor when it contains a GitHub issue reference to an issue with the label `enhancement` and to be a patch when
it contains a GitHub issue reference to an issue without the label `enhancement`.

### Gitea

The Gitea DSL is much like the GitHub DSL, you just need to provide the Gitea host name

```groovy
gver {
    issueTracker Gitea {
        host = "gitea.example.com"
        repo = "dmfs/gver"  // account/repo
        if (project.hasProperty("GITEA_API_TOKEN")) { // put the api token into your global gradle properties, never under version control
            accessToken = GITEA_API_TOKEN
        }
    }
    ...
}
```

## Dirty working trees

At present a dirty working tree always results in a pre-release version. Depending on the last tag, either the pre-release or the minor version
is incremented. This prevents accidental relase builds when after a file has been changed.

## License

Copyright 2022 dmfs GmbH

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

