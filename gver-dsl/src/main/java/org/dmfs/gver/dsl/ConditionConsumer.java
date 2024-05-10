package org.dmfs.gver.dsl;

import groovy.lang.Closure;


public interface ConditionConsumer
{
    void when(Closure<?> condition);

    /**
     * This pretends to be a property to allow calling it without parentheses
     */
    Void getOtherwise();
}
