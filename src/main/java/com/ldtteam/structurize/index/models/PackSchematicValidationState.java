package com.ldtteam.structurize.index.models;

import com.ldtteam.structurize.index.PackSchematicValidationCollector;
import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Holds the per-schematic result of a validation run, keyed by
 * {@link PackTypeSchematicRequirementSeverity}.
 *
 * <p>Unlike {@link PackValidationState} (which holds pack-level warnings and errors),
 * this class captures issues found against individual schematics by
 * {@link com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirement}s.
 *
 * <p>Instances are written to via {@link #apply(PackSchematicValidationCollector)}, which requires
 * a {@link PackSchematicValidationCollector} that can only be constructed within
 * {@code com.ldtteam.structurize.index}.
 */
public class PackSchematicValidationState
{
    private static final Codec<Map<PackTypeSchematicRequirementSeverity, List<Component>>> ISSUES_CODEC =
        Codec.unboundedMap(
            Codec.STRING.xmap(PackTypeSchematicRequirementSeverity::valueOf, Enum::name),
            ComponentSerialization.CODEC.listOf()
        );

    public static final Codec<PackSchematicValidationState> CODEC = ISSUES_CODEC.xmap(issues -> {
        final PackSchematicValidationState state = new PackSchematicValidationState();
        state.issues = Map.copyOf(issues);
        return state;
    }, s -> s.issues);

    private Map<PackTypeSchematicRequirementSeverity, List<Component>> issues = Collections.emptyMap();

    /**
     * Commits the issues accumulated in the given {@link PackSchematicValidationCollector} into
     * this state, replacing any previously stored results.
     *
     * <p>Only classes within {@code com.ldtteam.structurize.index} can create a
     * {@link PackSchematicValidationCollector}, so this method cannot be meaningfully called by
     * external code.
     *
     * @param collector the collector holding the accumulated issues
     */
    public void apply(final PackSchematicValidationCollector collector)
    {
        this.issues = Map.copyOf(collector.getIssues());
    }

    /**
     * Returns the full issues map, keyed by severity.
     *
     * @return an unmodifiable map of severity to issue messages
     */
    public Map<PackTypeSchematicRequirementSeverity, List<Component>> getIssues()
    {
        return issues;
    }

    /**
     * Returns all issue messages for the given severity, or an empty list if none exist.
     *
     * @param severity the severity to query
     * @return the list of issue messages for the given severity, never {@code null}
     */
    public List<Component> getIssues(final PackTypeSchematicRequirementSeverity severity)
    {
        return issues.getOrDefault(severity, Collections.emptyList());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof PackSchematicValidationState other))
        {
            return false;
        }
        return issues.equals(other.issues);
    }

    @Override
    public int hashCode()
    {
        return issues.hashCode();
    }
}