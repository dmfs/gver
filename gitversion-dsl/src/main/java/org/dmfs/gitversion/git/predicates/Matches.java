package org.dmfs.gitversion.git.predicates;

import java.util.function.Predicate;
import java.util.regex.Pattern;


/**
 * A {@link Predicate} that is satisfied by {@link CharSequence}s matching a certain regular expression pattern.
 */
public final class Matches implements Predicate<CharSequence>
{
    private final Pattern mPattern;


    public Matches(String pattern)
    {
        this(Pattern.compile(pattern));
    }


    public Matches(Pattern pattern)
    {
        mPattern = pattern;
    }


    @Override
    public boolean test(CharSequence testedInstance)
    {
        return mPattern.matcher(testedInstance).matches();
    }
}
