package com.ldtteam.structurize.index.packtypes.models;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.constants.TranslationConstants.*;

/**
 * A requirement that the pack as a whole must satisfy.
 *
 * <p>Requirements assert that one or more schematics exist in the pack matching all the
 * specified criteria:
 * <ul>
 *     <li>A specific schematic name</li>
 *     <li>A specific schematic path</li>
 *     <li>A specific anchor block type</li>
 * </ul>
 *
 * <p>If {@link #requiredLevelCount} is set, matching schematics must collectively cover each
 * level from 1 through N. For example, a count of 3 requires level 1, 2, and 3 to each be
 * present among matching schematics. If not set, at least one matching schematic at any level
 * is sufficient.
 *
 * <p>By default, requirements are mandatory. Use {@link Builder#optional()} to mark a requirement
 * as optional — it will still appear in the UI but will not be flagged as a hard error.
 */
public class PackTypeRequirement
{
    /**
     * If non-null, a schematic with this name must exist in the pack.
     */
    @Nullable
    private final String requiredName;

    /**
     * If non-null, a schematic at this path must exist in the pack.
     */
    @Nullable
    private final String requiredPath;

    /**
     * If non-null, matching schematics must collectively cover each level from 1 through this value.
     * If null, any single matching schematic at any level satisfies the requirement.
     */
    @Nullable
    private final Integer requiredLevelCount;

    /**
     * If non-null, a schematic with this anchor block type must exist in the pack.
     */
    @Nullable
    private final BlockState requiredAnchor;

    /**
     * Whether this requirement is optional. Optional requirements are shown in the UI but not flagged as mandatory.
     */
    private final boolean optional;

    private PackTypeRequirement(
        final @Nullable String requiredName,
        final @Nullable String requiredPath,
        final @Nullable Integer requiredLevelCount,
        final @Nullable BlockState requiredAnchor,
        final boolean optional)
    {
        this.requiredName = requiredName;
        this.requiredPath = requiredPath;
        this.requiredLevelCount = requiredLevelCount;
        this.requiredAnchor = requiredAnchor;
        this.optional = optional;
    }

    @Nullable
    public String getRequiredName()
    {
        return requiredName;
    }

    @Nullable
    public String getRequiredPath()
    {
        return requiredPath;
    }

    @Nullable
    public Integer getRequiredLevelCount()
    {
        return requiredLevelCount;
    }

    @Nullable
    public BlockState getRequiredAnchor()
    {
        return requiredAnchor;
    }

    public boolean isOptional()
    {
        return optional;
    }

    /**
     * Builds a {@link Component} describing this requirement's failure when no matching schematic
     * exists at all, including whether it was required or optional.
     *
     * @return the failure message component
     */
    public Component getFailureDescription()
    {
        return Component.translatable(optional ? PACK_TYPE_VALIDATION_OPTIONAL_SCHEMATIC : PACK_TYPE_VALIDATION_REQUIRED_SCHEMATIC, buildCriteria());
    }

    /**
     * Builds a {@link Component} describing a missing level for a requirement that is otherwise
     * partially satisfied — i.e. some matching schematics exist, but the given level is absent.
     *
     * @param missingLevel the level number that is missing
     * @return the missing-level message component
     */
    public Component getMissingLevelDescription(final int missingLevel)
    {
        return Component.translatable(PACK_TYPE_VALIDATION_MISSING_LEVEL, missingLevel, buildCriteria());
    }

    private Component buildCriteria()
    {
        final List<MutableComponent> parts = new ArrayList<>();
        if (requiredName != null)
        {
            parts.add(Component.translatable(PACK_TYPE_VALIDATION_REQUIREMENT_PART_NAME, requiredName));
        }
        if (requiredPath != null)
        {
            parts.add(Component.translatable(PACK_TYPE_VALIDATION_REQUIREMENT_PART_PATH, requiredPath));
        }
        if (requiredAnchor != null)
        {
            parts.add(Component.translatable(PACK_TYPE_VALIDATION_REQUIREMENT_PART_ANCHOR, requiredAnchor));
        }

        final Component and = Component.translatable(PACK_TYPE_VALIDATION_REQUIREMENT_PART_AND);
        return parts.stream().reduce((a, b) -> a.append(" ").append(and).append(" ").append(b)).orElse(Component.empty());
    }

    /**
     * Builder for {@link PackTypeRequirement}.
     */
    public static class Builder
    {
        /**
         * @see PackTypeRequirement#requiredName
         */
        @Nullable
        private String requiredName;

        /**
         * @see PackTypeRequirement#requiredPath
         */
        @Nullable
        private String requiredPath;

        /**
         * @see PackTypeRequirement#requiredLevelCount
         */
        @Nullable
        private Integer requiredLevelCount;

        /**
         * @see PackTypeRequirement#requiredAnchor
         */
        @Nullable
        private BlockState requiredAnchor;

        /**
         * @see PackTypeRequirement#optional
         */
        private boolean optional = false;

        /**
         * Requires a schematic with the given name to exist in the pack.
         *
         * @param name the required schematic name
         * @return this builder
         */
        public Builder requiresName(final String name)
        {
            this.requiredName = name;
            return this;
        }

        /**
         * Requires a schematic at the given path to exist in the pack.
         *
         * @param path the required schematic path
         * @return this builder
         */
        public Builder requiresPath(final String path)
        {
            this.requiredPath = path;
            return this;
        }

        /**
         * Requires that matching schematics cover each level from 1 through {@code count}.
         * For example, a count of 3 means level 1, 2, and 3 must each be present among matching
         * schematics. If not called, any single matching schematic at any level is sufficient.
         *
         * @param count the number of consecutive levels required, starting from 1
         * @return this builder
         */
        public Builder requiresLevelCount(final int count)
        {
            this.requiredLevelCount = count;
            return this;
        }

        /**
         * Requires a schematic with the given anchor block type to exist in the pack.
         *
         * @param anchor the required anchor block state
         * @return this builder
         */
        public Builder requiresAnchor(final BlockState anchor)
        {
            this.requiredAnchor = anchor;
            return this;
        }

        /**
         * Marks this requirement as optional. It will still appear in the UI, but will not be flagged as mandatory.
         *
         * @return this builder
         */
        public Builder optional()
        {
            this.optional = true;
            return this;
        }

        /**
         * Builds the {@link PackTypeRequirement}.
         *
         * @return the constructed requirement
         */
        public PackTypeRequirement build()
        {
            return new PackTypeRequirement(requiredName, requiredPath, requiredLevelCount, requiredAnchor, optional);
        }
    }
}
