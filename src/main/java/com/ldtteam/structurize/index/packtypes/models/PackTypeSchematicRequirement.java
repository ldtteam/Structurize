package com.ldtteam.structurize.index.packtypes.models;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.BlockPosUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.ldtteam.structurize.api.constants.Constants.GROUNDLEVEL_TAG;
import static com.ldtteam.structurize.api.constants.TranslationConstants.ANCHOR_POS_OUTSIDE_SCHEMATIC;
import static com.ldtteam.structurize.api.constants.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;
import static com.ldtteam.structurize.api.constants.TranslationConstants.PACK_TYPE_VALIDATION_ANCHOR_NO_GROUND_LEVEL;
import static com.ldtteam.structurize.api.constants.TranslationConstants.PACK_TYPE_VALIDATION_MULTIPLE_ANCHORS;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * A requirement checked against individual schematics to determine whether they are "valid".
 *
 * <p>Each requirement may match globally or be scoped to specific schematics via filters:
 * <ul>
 *     <li>Schematic name</li>
 *     <li>Schematic path</li>
 *     <li>Schematic level</li>
 *     <li>Anchor block type</li>
 *     <li>One or more tags on the anchor block</li>
 *     <li>Any number of custom {@link SchematicPredicate}s</li>
 * </ul>
 *
 * <p>Matched schematics are then validated against:
 * <ul>
 *     <li>One or more required tags on the anchor block</li>
 *     <li>One or more blocks that must be present in the schematic</li>
 *     <li>Any number of custom {@link SchematicPredicate}s</li>
 * </ul>
 *
 * <p>Each check carries an optional {@link Component} message override. If absent, a default
 * message is generated (e.g. including the tag name or block name). For additional checks
 * the message is mandatory since no meaningful default can be generated.
 *
 * <p>Each requirement carries a {@link PackTypeSchematicRequirementSeverity severity level} indicating
 * how problematic a violation is.
 */
public class PackTypeSchematicRequirement
{
    /**
     * Fires when a schematic has an anchor block but is missing the {@code groundlevel} tag.
     * The ground level will default to the block below the anchor, which may not be intended.
     */
    public static final PackTypeSchematicRequirement ANCHOR_NO_GROUND_LEVEL = new PackTypeSchematicRequirement.Builder()
        .matchesAdditionalCheck((blueprint, details) -> blueprint.getBlockInfoAsList()
            .stream()
            .anyMatch(blockInfo -> blockInfo.getTileEntityData() != null && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA)))
        .requiresTag(GROUNDLEVEL_TAG, Component.translatable(PACK_TYPE_VALIDATION_ANCHOR_NO_GROUND_LEVEL))
        .build();

    /**
     * Fires when a schematic has more than one anchor block and no explicit anchor position is set,
     * making the anchor ambiguous.
     */
    public static final PackTypeSchematicRequirement MULTIPLE_ANCHORS = new PackTypeSchematicRequirement.Builder()
        .requiresAdditionalCheck(
            (blueprint, details) -> details.getAnchor() != null || blueprint.getBlockInfoAsList()
                .stream()
                .filter(blockInfo -> blockInfo.getTileEntityData() != null && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
                .count() <= 1,
            Component.translatable(PACK_TYPE_VALIDATION_MULTIPLE_ANCHORS))
        .markAsError()
        .build();

    /**
     * Fires when a schematic has an explicit anchor position that lies outside its bounding box.
     */
    public static final PackTypeSchematicRequirement ANCHOR_OUTSIDE_BOUNDS = new PackTypeSchematicRequirement.Builder()
        .requiresAdditionalCheck(
            (blueprint, details) -> details.getAnchor() == null
                || BlockPosUtil.isInbetween(details.getAnchor(), details.getPos1(), details.getPos2()),
            Component.translatable(ANCHOR_POS_OUTSIDE_SCHEMATIC))
        .markAsError()
        .build();

    /**
     * Fires when a schematic's bounding box exceeds the configured {@code schematicBlockLimit}.
     */
    public static final PackTypeSchematicRequirement EXCEEDS_BLOCK_LIMIT = new PackTypeSchematicRequirement.Builder()
        .requiresAdditionalCheck(
            (blueprint, details) -> {
                final BoundingBox box = BoundingBox.fromCorners(details.getPos1(), details.getPos2());
                return (long) box.getXSpan() * box.getYSpan() * box.getZSpan() <= Structurize.getConfig().getServer().schematicBlockLimit.get();
            },
            () -> Component.translatable(MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get()))
        .markAsError()
        .build();

    /**
     * A required anchor tag paired with an optional message override.
     * If {@code message} is {@code null} a default is generated from the tag name.
     */
    public record TagCheck(@NotNull String tag, @Nullable Component message) {}

    /**
     * A required block paired with its minimum count and an optional message override.
     * If {@code message} is {@code null} a default is generated from the block name and counts.
     */
    public record BlockCheck(@NotNull BlockState block, int count, @Nullable Component message) {}

    /**
     * A required additional check paired with a mandatory failure message, since no meaningful
     * default can be generated for an arbitrary predicate.
     */
    public record AdditionalCheck(@NotNull SchematicPredicate predicate, @NotNull Supplier<Component> message) {}

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
     * If non-null, this requirement only applies to schematics whose level matches this value.
     */
    @Nullable
    private final Integer matchesLevel;

    /**
     * If non-null, this requirement only applies to schematics with this anchor block type.
     */
    @Nullable
    private final BlockState matchesAnchor;

    /**
     * If non-empty, this requirement only applies to schematics that have all of these tags on their anchor block.
     */
    @NotNull
    private final List<String> matchesTags;

    /**
     * If non-empty, this requirement only applies to schematics for which all of these predicates
     * return {@code true}. This is the blueprint-level equivalent of the tag/name/path/level/anchor
     * filter criteria, for cases that cannot be expressed with those simpler filters.
     */
    @NotNull
    private final List<SchematicPredicate> matchesAdditionalChecks;

    /**
     * Tags that must be present on the anchor block of any matched schematic.
     */
    @NotNull
    private final List<TagCheck> requiredTags;

    /**
     * Blocks that must be present in any matched schematic, with their minimum required counts.
     */
    @NotNull
    private final List<BlockCheck> requiredBlocks;

    /**
     * Custom predicates evaluated against any matched schematic, each with a mandatory failure message.
     */
    @NotNull
    private final List<AdditionalCheck> requiredAdditionalChecks;

    /**
     * The severity of a violation of this requirement.
     */
    private final PackTypeSchematicRequirementSeverity warningSeverity;

    private PackTypeSchematicRequirement(
        final @Nullable String matchesName,
        final @Nullable String matchesPath,
        final @Nullable Integer matchesLevel,
        final @Nullable BlockState matchesAnchor,
        final @NotNull List<String> matchesTags,
        final @NotNull List<SchematicPredicate> matchesAdditionalChecks,
        final @NotNull List<TagCheck> requiredTags,
        final @NotNull List<BlockCheck> requiredBlocks,
        final @NotNull List<AdditionalCheck> requiredAdditionalChecks,
        final PackTypeSchematicRequirementSeverity warningSeverity)
    {
        this.matchesName = matchesName;
        this.matchesPath = matchesPath;
        this.matchesLevel = matchesLevel;
        this.matchesAnchor = matchesAnchor;
        this.matchesTags = matchesTags;
        this.matchesAdditionalChecks = matchesAdditionalChecks;
        this.requiredTags = requiredTags;
        this.requiredBlocks = requiredBlocks;
        this.requiredAdditionalChecks = requiredAdditionalChecks;
        this.warningSeverity = warningSeverity;
    }

    @Nullable
    public String getMatchesName()
    {
        return matchesName;
    }

    @Nullable
    public String getMatchesPath()
    {
        return matchesPath;
    }

    @Nullable
    public Integer getMatchesLevel()
    {
        return matchesLevel;
    }

    @Nullable
    public BlockState getMatchesAnchor()
    {
        return matchesAnchor;
    }

    @NotNull
    public List<String> getMatchesTags()
    {
        return matchesTags;
    }

    @NotNull
    public List<SchematicPredicate> getMatchesAdditionalChecks()
    {
        return matchesAdditionalChecks;
    }

    @NotNull
    public List<TagCheck> getRequiredTags()
    {
        return requiredTags;
    }

    @NotNull
    public List<BlockCheck> getRequiredBlocks()
    {
        return requiredBlocks;
    }

    @NotNull
    public List<AdditionalCheck> getRequiredAdditionalChecks()
    {
        return requiredAdditionalChecks;
    }

    public PackTypeSchematicRequirementSeverity getWarningSeverity()
    {
        return warningSeverity;
    }

    /**
     * Builder for {@link PackTypeSchematicRequirement}.
     */
    public static class Builder
    {
        @Nullable
        private String matchesName;

        @Nullable
        private String matchesPath;

        @Nullable
        private Integer matchesLevel;

        @Nullable
        private BlockState matchesAnchor;

        @NotNull
        private final List<String> matchesTags = new ArrayList<>();

        @NotNull
        private final List<SchematicPredicate> matchesAdditionalChecks = new ArrayList<>();

        @NotNull
        private final List<TagCheck> requiredTags = new ArrayList<>();

        @NotNull
        private final List<BlockCheck> requiredBlocks = new ArrayList<>();

        @NotNull
        private final List<AdditionalCheck> requiredAdditionalChecks = new ArrayList<>();

        private PackTypeSchematicRequirementSeverity warningSeverity = PackTypeSchematicRequirementSeverity.INFORMATIONAL;

        /**
         * Restricts this requirement to schematics whose name matches the given value.
         */
        public Builder matchesName(final String name)
        {
            this.matchesName = name;
            return this;
        }

        /**
         * Restricts this requirement to schematics whose path matches the given value.
         */
        public Builder matchesPath(final String path)
        {
            this.matchesPath = path;
            return this;
        }

        /**
         * Restricts this requirement to schematics whose level matches the given value.
         */
        public Builder matchesLevel(final Integer level)
        {
            this.matchesLevel = level;
            return this;
        }

        /**
         * Restricts this requirement to schematics whose level matches the given value.
         */
        public Builder matchesLevel(final Integer level)
        {
            this.matchesLevel = level;
            return this;
        }

        /**
         * Restricts this requirement to schematics that use the given anchor block type.
         */
        public Builder matchesAnchor(final BlockState anchor)
        {
            this.matchesAnchor = anchor;
            return this;
        }

        /**
         * Restricts this requirement to schematics that have the given tag on their anchor block.
         * May be called multiple times; all tags must be present for the requirement to apply.
         */
        public Builder matchesTag(final String tag)
        {
            this.matchesTags.add(tag);
            return this;
        }

        /**
         * Restricts this requirement to schematics for which the given predicate returns {@code true}.
         * May be called multiple times; all predicates must match for the requirement to apply.
         */
        public Builder matchesAdditionalCheck(final SchematicPredicate predicate)
        {
            this.matchesAdditionalChecks.add(predicate);
            return this;
        }

        /**
         * Requires the anchor block of a matched schematic to have the given tag.
         * Uses a default generated failure message.
         */
        public Builder requiresTag(final String tag)
        {
            return requiresTag(tag, null);
        }

        /**
         * Requires the anchor block of a matched schematic to have the given tag.
         * Uses the provided message on failure instead of the default generated one.
         */
        public Builder requiresTag(final String tag, final Component message)
        {
            this.requiredTags.add(new TagCheck(tag, message));
            return this;
        }

        /**
         * Requires at least one instance of the given block to be present in a matched schematic.
         * Uses a default generated failure message.
         */
        public Builder requiresBlock(final BlockState block)
        {
            return requiresBlock(block, 1, null);
        }

        /**
         * Requires at least one instance of the given block to be present in a matched schematic.
         * Uses the provided message on failure instead of the default generated one.
         */
        public Builder requiresBlock(final BlockState block, final Component message)
        {
            return requiresBlock(block, 1, message);
        }

        /**
         * Requires at least {@code count} instances of the given block to be present in a matched schematic.
         * Uses a default generated failure message.
         */
        public Builder requiresBlock(final BlockState block, final int count)
        {
            return requiresBlock(block, count, null);
        }

        /**
         * Requires at least {@code count} instances of the given block to be present in a matched schematic.
         * Uses the provided message on failure instead of the default generated one.
         */
        public Builder requiresBlock(final BlockState block, final int count, final @Nullable Component message)
        {
            this.requiredBlocks.add(new BlockCheck(block, count, message));
            return this;
        }

        /**
         * Adds a custom predicate evaluated against any matched schematic.
         * The message is mandatory since no meaningful default can be generated for an arbitrary predicate.
         */
        public Builder requiresAdditionalCheck(final SchematicPredicate predicate, final Component message)
        {
            this.requiredAdditionalChecks.add(new AdditionalCheck(predicate, () -> message));
            return this;
        }

        /**
         * Adds a custom predicate evaluated against any matched schematic, with a lazily-evaluated
         * message supplier. Use this when the message content depends on runtime state (e.g. config values)
         * that is not available at class initialization time.
         */
        public Builder requiresAdditionalCheck(final SchematicPredicate predicate, final Supplier<Component> message)
        {
            this.requiredAdditionalChecks.add(new AdditionalCheck(predicate, message));
            return this;
        }

        /**
         * Sets the warning severity to {@link PackTypeSchematicRequirementSeverity#ISSUE}.
         */
        public Builder markAsIssue()
        {
            this.warningSeverity = PackTypeSchematicRequirementSeverity.ISSUE;
            return this;
        }

        /**
         * Sets the warning severity to {@link PackTypeSchematicRequirementSeverity#ERROR}.
         */
        public Builder markAsError()
        {
            this.warningSeverity = PackTypeSchematicRequirementSeverity.ERROR;
            return this;
        }

        /**
         * Builds the {@link PackTypeSchematicRequirement}.
         */
        public PackTypeSchematicRequirement build()
        {
            return new PackTypeSchematicRequirement(
                matchesName,
                matchesPath,
                matchesLevel,
                matchesAnchor,
                matchesTags,
                matchesAdditionalChecks,
                requiredTags,
                requiredBlocks,
                requiredAdditionalChecks,
                warningSeverity);
        }
    }
}
