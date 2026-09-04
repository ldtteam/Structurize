package com.ldtteam.structurize.api.util;

/**
 * Predicate for blueprint placement checks, which require three inputs.
 */
@FunctionalInterface
public interface TriPredicate<A, B, C>
{
    boolean test(A first, B second, C third);
}
