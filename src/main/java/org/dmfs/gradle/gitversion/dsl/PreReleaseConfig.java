package org.dmfs.gradle.gitversion.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Closure;


public final class PreReleaseConfig
{
    public final List<Function<String, ? extends Optional<String>>> mBranchConfigs = new ArrayList<>();


    public BranchConfig on(Pattern branchPattern)
    {
        return preReleaseClosure -> mBranchConfigs.add(branch -> {
            Matcher matcher = branchPattern.matcher(branch);
            if (matcher.matches())
            {
                preReleaseClosure.setResolveStrategy(Closure.DELEGATE_FIRST);
                preReleaseClosure.setDelegate(matcher);
                return Optional.of(preReleaseClosure.call(matcher)
                    .toString()
                    .replace('_', '-')
                    .replaceAll("[^.a-zA-Z0-9-]", ""));
            }
            return Optional.empty();
        });
    }


    public interface BranchConfig
    {
        void use(Closure<?> preReleaseClosure);
    }
}