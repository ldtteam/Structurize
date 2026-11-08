package com.ldtteam.structurize.index;

import com.ldtteam.structurize.index.packtypes.models.PackTypeSchematicRequirementSeverity;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A temporary collector that accumulates per-schematic validation issues and can commit them
 * to a {@link com.ldtteam.structurize.index.models.PackSchematicValidationState}.
 *
 * <p>The constructor is package-private, so only classes within {@code com.ldtteam.structurize.index}
 * can create instances. This prevents external code from getting write access to a
 * {@link com.ldtteam.structurize.index.models.PackSchematicValidationState}.
 */
public final class PackSchematicValidationCollector
{
    private final Map<PackTypeSchematicRequirementSeverity, List<Component>> issues = new EnumMap<>(PackTypeSchematicRequirementSeverity.class);

    PackSchematicValidationCollector() {}

    /**
     * Appends an issue message at the given severity level.
     *
     * @param severity the severity of the issue
     * @param message  the issue message
     */
    public void addIssue(final PackTypeSchematicRequirementSeverity severity, final Component message)
    {
        issues.computeIfAbsent(severity, k -> new ArrayList<>()).add(message);
    }

    /**
     * Returns the accumulated issues map.
     *
     * @return the issues map
     */
    public Map<PackTypeSchematicRequirementSeverity, List<Component>> getIssues()
    {
        return issues;
    }
}
