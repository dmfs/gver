package org.dmfs.gver.dsl.utils;

import groovy.lang.Closure;
import org.dmfs.jems2.Function;
import org.dmfs.jems2.Procedure;

public final class LambdaClosure<Result, Delegate> extends Closure<Result>
{
    private final Function<Delegate, Result> mDelegate;

    public LambdaClosure(Object owner, Function<Delegate, Result> delegate)
    {
        super(owner);
        mDelegate = delegate;
    }

    public LambdaClosure(Object owner, Procedure<Delegate> delegate)
    {
        this(owner, delegate1 -> {
            delegate.process(delegate1);
            return null;
        });
    }

    @Override
    public Result call()
    {
        return mDelegate.value((Delegate) getDelegate());
    }
}
