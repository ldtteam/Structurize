package com.ldtteam.structurize.index.packtypes.models;

/**
 * Severity level for a schematic requirement violation.
 */
public enum PackTypeSchematicRequirementSeverity
{
    /**
     * Purely informational — no immediate problem, but worth noting.
     *
     * <p>Example: No {@code groundlevel} tag on a schematic <em>with</em> an anchor block.
     * This is fine because the ground level defaults to the block below the anchor.
     */
    INFORMATIONAL,

    /**
     * A potential problem — not immediately broken, but may not behave as intended.
     *
     * <p>Example: No {@code groundlevel} tag on a schematic <em>without</em> an anchor block.
     * This causes the bottom of the full schematic to be used as the ground level.
     */
    ISSUE,

    /**
     * A hard error that will cause problems in-game and must be fixed before release. These issues are blocking and will not allow a scan to be made!
     *
     * <p>Example: Two anchor blocks in a schematic without an explicitly defined anchor position.
     */
    ERROR
}
