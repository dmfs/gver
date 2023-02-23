package org.dmfs.gver.dsl;

import groovy.lang.Closure;


public interface SuffixPattern
{
    void when(Closure<?> condition);
}
