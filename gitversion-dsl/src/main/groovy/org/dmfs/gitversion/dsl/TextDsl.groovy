package org.dmfs.gitversion.dsl

import org.dmfs.gitversion.dsl.IssueDsl
import org.dmfs.gitversion.dsl.IssueTracker

import java.util.function.Predicate
import java.util.regex.Matcher

final class TextDsl {


    private final Optional<IssueTracker> mTracker;


    TextDsl(Optional<IssueTracker> tracker) {
        mTracker = tracker;
    }


    Predicate<Matcher> where(String group, Closure<Predicate<String>> is) {
        is.setResolveStrategy(Closure.DELEGATE_FIRST);
        return (m) -> {
            if (m.group(group) != null) {
                String match = m.group(group);
                is.setDelegate(new IssueDsl(mTracker));
                if (is.call().test(match)) {
                    return true;
                }
            }
            return false;
        };
    }
}
