# gitversion

[![Build Status](https://travis-ci.com/dmfs/gitversion.svg?branch=main)](https://travis-ci.com/dmfs/gitversion)
[![codecov](https://codecov.io/gh/dmfs/gitversion/branch/main/graph/badge.svg?token=Nkc6f2B7rO)](https://codecov.io/gh/dmfs/gitversion)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/dmfs/gitversion.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/dmfs/gitversion/context:java)

Gradle versioning based on your Git history.

# Example

In order to use the plugin it needs to be configured. Currently, there is no default configuration.

The following example considers commits to be breaking changes when the commit message contains either the hashtags `#major` or `#break`. If the commit message
contains neither, the commit message is searched for the pattern `#\d` and, in case one is found, a matching issue is looked up in the Github repository
"dmfs/jems". If an issue is found, the change type is determined by whether the issue has a `bug` tag or not. If no Github issue was found, the change type is
considered to be a minor change when the commit message contains either the pattern `#feature\b` or
`(?i)\b(implement(s|ed)?|close[sd]?) #\d+\b`, and a patch when it contains `(?i)\b(fix(e[sd])?|resolve[sd]?) #\d+\b`.

```groovy
gitVersion {
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
            commitMessage containsIssue { issue -> issue.labels?.every { it.name != "bug" } }
        }
        are patch when {
            commitMessage containsIssue { issue -> issue.labels?.any { it.name == "bug" } }
        }
        are minor when {
            commitMessage contains("#feature\\b")
        }
        are minor when {
            commitMessage contains("(?i)\\b(implement(s|ed)?|close[sd]?) #\\d+\\b")
        }
        are patch when {
            commitMessage contains("(?i)\\b(fix(e[sd])?|resolve[sd]?) #\\d+\\b")
        }
        otherwise patch
    }
    preReleases {
        // pre-release on the main branch are versioned like a.b.c-beta.x
        on ~/main/ use { "beta" }

        // pre-release on the feature branches are versioned like a.b.c-alpha-<feature-name>.x
        on ~/feature\/(?<name>.*)/ use { "alpha-${group('name')}" }

        // every other pre-release is versioned like a.b.c-SNAPSHOT.x
        on ~/.*/ use { "SHAPSHOT" }
    }
    // releases can only be made on the main branch, on other branches the `gitRelease` task will fail
    releaseBranchPattern ~/main$/ // defaults to ~/(main|master)$/
}
```

## new in version 0.6

The plugin now supports GitHub authentication to circumvent the unauthorized API quota. To generate a token go to https://github.com/settings/tokens
and create a token without additional scopes (leave all boxes blank).

Store the token in your global `gradle.properties` under a name like `GITHUB_API_TOKEN` and add an entry like

```
    issueTracker GitHub {
        repo = "dmfs/jems"
        if (project.hasProperty("GITHUB_API_TOKEN")) {
            accessToken = GITHUB_API_TOKEN // should be stored in your global or local gradle properties
        }
    }
```

to your `gitVersion` configuration (remove any old `githubRepo` entry). The `if`-clause ensures that users without that property still can build the 
project.

DON'T PUT THE FILE CONTAINING THE TOKEN UNDER VERSION CONTROL!

If you don't want to use an access token prepare for build errors due to the API quota. In this case the DSL now looks just like

```
    issueTracker GitHub {
        repo = "dmfs/jems"
    }
```

Change the DSL method for parsing issues from `referencesGithubIssue` to `containsIssue`. Note, that, despite the name, this method currently
only checks the first reference to an issue that matches the regex `#\d+`

## License

Copyright 2022 dmfs GmbH

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

