package com.ldtteam.structurize.index.packtypes.models;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.IBlueprintDetails;

/**
 * A predicate evaluated against a schematic's {@link Blueprint} and its associated {@link IBlueprintDetails}.
 *
 * <p>Used both as a filter (to determine whether a {@link PackTypeSchematicRequirement} applies to a
 * given schematic) and as a content check (to determine whether a matched schematic is valid).
 */
@FunctionalInterface
public interface SchematicPredicate
{
    /**
     * Evaluates this predicate against the given blueprint and its details.
     *
     * @param blueprint the blueprint data
     * @param details   the blueprint details providing path, name, level, and anchor
     * @return {@code true} if the predicate is satisfied
     */
    boolean test(Blueprint blueprint, IBlueprintDetails details);
}