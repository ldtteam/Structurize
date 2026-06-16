package com.ldtteam.structurize.index.packtypes.models;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the type of schematic pack, including its display name and the requirements that packs
 * and individual schematics of this type must satisfy.
 *
 * <p>Instances are registered via NeoForge's registry using {@link com.ldtteam.structurize.index.packtypes.PackTypesRegistry}.
 * Use {@link Builder} to construct instances.
 */
public class PackType
{
    private final ResourceLocation id;

    private final Component name;

    private final List<PackTypeRequirement> packRequirements;

    private final List<PackTypeSchematicRequirement> schematicRequirements;

    private PackType(
        final ResourceLocation id,
        final Component name,
        final List<PackTypeRequirement> packRequirements,
        final List<PackTypeSchematicRequirement> schematicRequirements)
    {
        this.id = id;
        this.name = name;
        this.packRequirements = packRequirements;
        this.schematicRequirements = schematicRequirements;
    }

    /**
     * Returns the registry ID of this pack type.
     *
     * @return the resource location identifier
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * Returns the localized display name of this pack type shown in the UI.
     *
     * @return the display name component
     */
    public Component getName()
    {
        return name;
    }

    /**
     * Returns the pack-level requirements that a {@link com.ldtteam.structurize.index.models.Pack} of this type must satisfy.
     * These are checked against the set of schematics as a whole (e.g., required schematic names and level counts).
     *
     * @return an unmodifiable list of pack requirements
     */
    public List<PackTypeRequirement> getPackRequirements()
    {
        return packRequirements;
    }

    /**
     * Returns the per-schematic requirements evaluated against individual schematics when a full validation is run.
     *
     * @return an unmodifiable list of schematic requirements
     */
    public List<PackTypeSchematicRequirement> getSchematicRequirements()
    {
        return schematicRequirements;
    }

    /**
     * Builder for {@link PackType}.
     */
    public static class Builder
    {
        private final ResourceLocation id;

        private Component name;

        private final List<PackTypeRequirement> packRequirements = new ArrayList<>();

        private final List<PackTypeSchematicRequirement> schematicRequirements = new ArrayList<>(List.of(
            PackTypeSchematicRequirement.ANCHOR_OUTSIDE_BOUNDS,
            PackTypeSchematicRequirement.EXCEEDS_BLOCK_LIMIT,
            PackTypeSchematicRequirement.MULTIPLE_ANCHORS,
            PackTypeSchematicRequirement.ANCHOR_NO_GROUND_LEVEL
        ));

        /**
         * Creates a builder for a pack type with the given registry ID.
         *
         * @param id the resource location that will be used to register this pack type
         */
        public Builder(final ResourceLocation id)
        {
            this.id = id;
        }

        /**
         * Sets the localized display name shown in the UI.
         *
         * @param name the display name component
         * @return this builder
         */
        public Builder withName(final Component name)
        {
            this.name = name;
            return this;
        }

        /**
         * Adds a pack-level requirement using the given builder.
         *
         * @param requirement the requirement builder to build and add
         * @return this builder
         */
        public Builder addPackRequirement(final PackTypeRequirement.Builder requirement)
        {
            this.packRequirements.add(requirement.build());
            return this;
        }

        /**
         * Adds a per-schematic requirement using the given builder.
         *
         * @param requirement the requirement builder to build and add
         * @return this builder
         */
        public Builder addSchematicRequirement(final PackTypeSchematicRequirement.Builder requirement)
        {
            this.schematicRequirements.add(requirement.build());
            return this;
        }

        /**
         * Builds the {@link PackType}.
         *
         * @return the constructed pack type
         */
        public PackType build()
        {
            return new PackType(this.id, this.name, this.packRequirements, this.schematicRequirements);
        }
    }
}
