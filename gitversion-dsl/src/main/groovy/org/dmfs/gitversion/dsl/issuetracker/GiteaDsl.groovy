package org.dmfs.gitversion.dsl.issuetracker

import java.util.function.Predicate

class GiteaDsl {
    static Predicate<Object> labeled(String label) {
        new Predicate<Object>() {
            @Override
            boolean test(Object issue) {
                issue.labels?.any { it.name == label }
            }
        }
    }
}
