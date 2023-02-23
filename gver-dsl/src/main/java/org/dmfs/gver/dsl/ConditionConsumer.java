package org.dmfs.gver.dsl;

import groovy.lang.Closure;


public interface ConditionConsumer
{
    void when(Closure<?> condition);
}
