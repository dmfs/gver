package org.dmfs.gitversion.dsl.utils;

import org.dmfs.jems2.Function;
import org.dmfs.jems2.Generator;
import org.dmfs.srcless.annotations.staticfactory.StaticFactories;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


@StaticFactories("Matchers")
public final class StdOutCaptured<T> extends TypeSafeDiagnosingMatcher<T>
{
    private final Function<Generator<String>, ? extends Matcher<? super T>> mDelegateFunction;


    public static <T> Matcher<T> stdoutCaptured(Function<Generator<String>, ? extends Matcher<? super T>> delegateFunction)
    {
        return new StdOutCaptured<>(delegateFunction);
    }


    public StdOutCaptured(Function<Generator<String>, ? extends Matcher<? super T>> delegateFunction)
    {
        mDelegateFunction = delegateFunction;
    }


    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription)
    {
        PrintStream old = System.out;
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);
            System.setOut(ps);
            Matcher<? super T> delegate = mDelegateFunction.value(() -> {
                ps.flush();
                return out.toString(StandardCharsets.UTF_8);
            });
            if (!delegate.matches(item))
            {
                delegate.describeMismatch(item, mismatchDescription);
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            mismatchDescription.appendText("context could not be created");
            mismatchDescription.appendValue(e);
            return false;
        }
        finally
        {
            System.setOut(old);
        }
    }


    @Override
    public void describeTo(Description description)
    {
        mDelegateFunction.value(() -> "StdOut").describeTo(description);

    }
}
