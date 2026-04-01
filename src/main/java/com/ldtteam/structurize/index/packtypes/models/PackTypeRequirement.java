package com.ldtteam.structurize.index.packtypes.models;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * A requirement that the pack as a whole must satisfy.
 *
 * <p>Requirements can assert that a schematic exists matching one of the following criteria:
 * <ul>
 *     <li>A specific schematic name</li>
 *     <li>A specific schematic path</li>
 *     <li>A specific anchor block type</li>
 * </ul>
 *
 * <p>By default, requirements are mandatory, shown with an exclamation mark in the UI.
 * Use {@link Builder#optional()} to display a requirement without marking it as mandatory.
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
     * If non-null, a schematic with this anchor block type must exist in the pack.
     */
    @Nullable
    private final BlockState requiredAnchor;

    /**
     * Whether this requirement is optional. Optional requirements are shown in the UI but not flagged as mandatory.
     */
    private final boolean optional;

    private PackTypeRequirement(final @Nullable String requiredName, final @Nullable String requiredPath, final @Nullable BlockState requiredAnchor, final boolean optional)
    {
        this.requiredName = requiredName;
        this.requiredPath = requiredPath;
        this.requiredAnchor = requiredAnchor;
        this.optional = optional;
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
            return new PackTypeRequirement(requiredName, requiredPath, requiredAnchor, optional);
        }
    }
}
