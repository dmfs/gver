package org.dmfs.gver.dsl.utils;

import org.dmfs.jems2.Fragile;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.Procedure;
import org.dmfs.srcless.annotations.staticfactory.StaticFactories;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.Closeable;
import java.io.IOException;


@StaticFactories("Matchers")
public final class Given<T, E> extends TypeSafeDiagnosingMatcher<T>
{
    private final Fragile<? extends E, Exception> mEnvGenerator;
    private final Function<? super E, ? extends Matcher<? super T>> mDelegateFunction;
    private final Procedure<? super E> mEnvTearDown;


    public Given(Fragile<? extends E, Exception> envGenerator,
        Function<? super E, ? extends Matcher<? super T>> delegateFunction)
    {
        this(envGenerator, delegateFunction, env -> {
            if (env instanceof Closeable)
            {
                try
                {
                    ((Closeable) env).close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        });
    }


    public Given(Fragile<? extends E, Exception> envGenerator,
        Function<? super E, ? extends Matcher<? super T>> delegateFunction,
        Procedure<? super E> envTearDown)
    {
        mEnvGenerator = envGenerator;
        mDelegateFunction = delegateFunction;
        mEnvTearDown = envTearDown;
    }


    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription)
    {
        E env = null;
        try
        {
            env = mEnvGenerator.value();
            Matcher<? super T> delegate = mDelegateFunction.value(env);
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
            if (env != null)
            {
                mEnvTearDown.process(env);
            }
        }
    }


    @Override
    public void describeTo(Description description)
    {
        E env = null;
        try
        {
            env = mEnvGenerator.value();
            mDelegateFunction.value(env).describeTo(description);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Can't create context", e);
        }
        finally
        {
            if (env != null)
            {
                mEnvTearDown.process(env);
            }
        }
    }
}
