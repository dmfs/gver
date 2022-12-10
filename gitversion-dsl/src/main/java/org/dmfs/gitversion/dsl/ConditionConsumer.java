package org.dmfs.gitversion.dsl;

import groovy.lang.Closure;


public interface ConditionConsumer
{
    void when(Closure<?> condition);
}
