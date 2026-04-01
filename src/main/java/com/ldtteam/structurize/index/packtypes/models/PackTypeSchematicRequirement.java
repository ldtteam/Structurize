package com.ldtteam.structurize.index.packtypes.models;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.ldtteam.structurize.api.constants.Constants.GROUNDLEVEL_TAG;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * A requirement checked against individual schematics to determine whether they are "valid".
 *
 * <p>Each requirement may match globally or be scoped to specific schematics via filters:
 * <ul>
 *     <li>Schematic name</li>
 *     <li>Schematic path</li>
 *     <li>Anchor block type</li>
 *     <li>One or more tags</li>
 * </ul>
 *
 * <p>Matched schematics are then validated against:
 * <ul>
 *     <li>One or more required tags on the anchor block</li>
 *     <li>One or more blocks that must be present in the schematic</li>
 *     <li>Any number of custom predicates operating on the full {@link Blueprint} data</li>
 * </ul>
 *
 * <p>Each requirement carries a {@link PackTypeSchematicRequirementSeverity severity level} indicating
 * how problematic a violation is.
 */
public class PackTypeSchematicRequirement
{
    public static final PackTypeSchematicRequirement ANCHOR_NO_GROUND_LEVEL = new PackTypeSchematicRequirement.Builder().addRequiredTag(GROUNDLEVEL_TAG)
        .addAdditionalBlueprintCheck(blueprint -> blueprint.getBlockInfoAsList()
            .stream()
            .filter(blockInfo -> blockInfo.hasTileEntityData() && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
            .toList()
            .size() > 1)
        .markAsIssue()
        .build();

    public static final PackTypeSchematicRequirement MULTIPLE_ANCHORS = new PackTypeSchematicRequirement.Builder().addRequiredTag(GROUNDLEVEL_TAG)
        .addAdditionalBlueprintCheck(blueprint -> blueprint.getBlockInfoAsList()
            .stream()
            .filter(blockInfo -> blockInfo.hasTileEntityData() && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
            .toList()
            .size() > 1)
        .markAsIssue()
        .build();

    /**
     * If non-null, this requirement only applies to schematics whose name matches this value.
     */
    @Nullable
    private final String matchesName;

    /**
     * If non-null, this requirement only applies to schematics whose path matches this value.
     */
    @Nullable
    private final String matchesPath;

    /**
     * If non-null, this requirement only applies to schematics with this anchor block type.
     */
    @Nullable
    private final BlockState matchesAnchor;

    /**
     * If non-null, this requirement only applies to schematics that have all of these tags.
     */
    @Nullable
    private final List<String> matchesTags;

    /**
     * Tags that must be present on the anchor block of any matched schematic.
     */
    @NotNull
    private final List<String> requiredTags;

    /**
     * Blocks that must be present in any matched schematic, mapped to their minimum required count.
     */
    @NotNull
    private final Map<BlockState, Integer> requiredBlocks;

    /**
     * Additional custom predicates evaluated against the full {@link Blueprint} of any matched schematic.
     * All predicates must return {@code true} for the schematic to be considered valid.
     */
    @NotNull
    private final List<Predicate<Blueprint>> additionalBlueprintChecks;

    /**
     * The severity of a violation of this requirement.
     */
    private final PackTypeSchematicRequirementSeverity warningSeverity;

    private PackTypeSchematicRequirement(
        final @Nullable String matchesName,
        final @Nullable String matchesPath,
        final @Nullable BlockState matchesAnchor,
        final @Nullable List<String> matchesTags,
        final @NotNull List<String> requiredTags,
        final @NotNull Map<BlockState, Integer> requiredBlocks,
        final @NotNull List<Predicate<Blueprint>> additionalBlueprintChecks,
        final PackTypeSchematicRequirementSeverity warningSeverity)
    {
        this.matchesName = matchesName;
        this.matchesPath = matchesPath;
        this.matchesAnchor = matchesAnchor;
        this.matchesTags = matchesTags;
        this.requiredTags = requiredTags;
        this.requiredBlocks = requiredBlocks;
        this.additionalBlueprintChecks = additionalBlueprintChecks;
        this.warningSeverity = warningSeverity;
    }

    /**
     * Builder for {@link PackTypeSchematicRequirement}.
     */
    public static class Builder
    {
        /**
         * @see PackTypeSchematicRequirement#matchesName
         */
        @Nullable
        private String matchesName;

        /**
         * @see PackTypeSchematicRequirement#matchesPath
         */
        @Nullable
        private String matchesPath;

        /**
         * @see PackTypeSchematicRequirement#matchesAnchor
         */
        @Nullable
        private BlockState matchesAnchor;

        /**
         * @see PackTypeSchematicRequirement#matchesTags
         */
        @NotNull
        private final List<String> matchesTags = new ArrayList<>();

        /**
         * @see PackTypeSchematicRequirement#requiredTags
         */
        @NotNull
        private final List<String> requiredTags = new ArrayList<>();

        /**
         * @see PackTypeSchematicRequirement#requiredBlocks
         */
        @NotNull
        private final Map<BlockState, Integer> requiredBlocks = new HashMap<>();

        /**
         * @see PackTypeSchematicRequirement#additionalBlueprintChecks
         */
        @NotNull
        private final List<Predicate<Blueprint>> additionalBlueprintChecks = new ArrayList<>();

        /**
         * @see PackTypeSchematicRequirement#warningSeverity
         */
        private PackTypeSchematicRequirementSeverity warningSeverity = PackTypeSchematicRequirementSeverity.INFORMATIONAL;

        /**
         * Restricts this requirement to schematics whose name matches the given value.
         *
         * @param name the schematic name to match
         * @return this builder
         */
        public Builder matchesName(final String name)
        {
            this.matchesName = name;
            return this;
        }

        /**
         * Restricts this requirement to schematics whose path matches the given value.
         *
         * @param path the schematic path to match
         * @return this builder
         */
        public Builder matchesPath(final String path)
        {
            this.matchesPath = path;
            return this;
        }

        /**
         * Restricts this requirement to schematics that use the given anchor block type.
         *
         * @param anchor the anchor block state to match
         * @return this builder
         */
        public Builder matchesAnchor(final BlockState anchor)
        {
            this.matchesAnchor = anchor;
            return this;
        }

        /**
         * Requires the anchor block of a matched schematic to have the given tag.
         * May be called multiple times to require several tags.
         *
         * @param tag the tag that must be present on the anchor block
         * @return this builder
         */
        public Builder addRequiredTag(final String tag)
        {
            this.requiredTags.add(tag);
            return this;
        }

        /**
         * Requires at least one instance of the given block to be present in a matched schematic.
         * Equivalent to {@code addRequiredBlock(block, 1)}.
         *
         * @param block the block that must be present
         * @return this builder
         */
        public Builder addRequiredBlock(final BlockState block)
        {
            return addRequiredBlock(block, 1);
        }

        /**
         * Requires at least {@code count} instances of the given block to be present in a matched schematic.
         *
         * @param block the block that must be present
         * @param count the minimum number of occurrences required
         * @return this builder
         */
        public Builder addRequiredBlock(final BlockState block, final int count)
        {
            this.requiredBlocks.put(block, count);
            return this;
        }

        /**
         * Adds a custom predicate evaluated against the full {@link Blueprint} of any matched schematic.
         * All added predicates must return {@code true} for the schematic to be considered valid.
         * May be called multiple times to add several checks.
         *
         * @param additionalCheck a predicate receiving the full blueprint data
         * @return this builder
         */
        public Builder addAdditionalBlueprintCheck(final Predicate<Blueprint> additionalCheck)
        {
            this.additionalBlueprintChecks.add(additionalCheck);
            return this;
        }

        /**
         * Sets the warning severity to {@link PackTypeSchematicRequirementSeverity#ISSUE}.
         *
         * @return this builder
         */
        public Builder markAsIssue()
        {
            this.warningSeverity = PackTypeSchematicRequirementSeverity.ISSUE;
            return this;
        }

        /**
         * Sets the warning severity to {@link PackTypeSchematicRequirementSeverity#ERROR}.
         *
         * @return this builder
         */
        public Builder markAsError()
        {
            this.warningSeverity = PackTypeSchematicRequirementSeverity.ERROR;
            return this;
        }

        /**
         * Builds the {@link PackTypeSchematicRequirement}.
         *
         * @return the constructed requirement
         */
        public PackTypeSchematicRequirement build()
        {
            return new PackTypeSchematicRequirement(matchesName,
                matchesPath,
                matchesAnchor,
                matchesTags,
                requiredTags,
                requiredBlocks,
                additionalBlueprintChecks,
                warningSeverity);
        }
    }
}
