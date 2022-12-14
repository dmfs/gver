package org.dmfs.gitversion.dsl;

import groovy.lang.Closure;


public interface SuffixPattern
{
    void when(Closure<?> condition);
}
