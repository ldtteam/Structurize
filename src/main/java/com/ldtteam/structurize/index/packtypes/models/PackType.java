package com.ldtteam.structurize.index.packtypes.models;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PackType
{
    private final ResourceLocation packId;

    private final Component name;

    private final List<PackTypeRequirement> packRequirements;

    private final List<PackTypeSchematicRequirement> schematicRequirements;

    private PackType(
        final ResourceLocation packId, final Component name, final List<PackTypeRequirement> packRequirements,
        final List<PackTypeSchematicRequirement> schematicRequirements)
    {
        this.packId = packId;
        this.name = name;
        this.packRequirements = packRequirements;
        this.schematicRequirements = schematicRequirements;
    }

    public static class Builder
    {
        private final ResourceLocation packId;

        private Component name;

        private final List<PackTypeRequirement> packRequirements = new ArrayList<>();

        private final List<PackTypeSchematicRequirement> schematicRequirements = new ArrayList<>();

        public Builder(final ResourceLocation packId)
        {
            this.packId = packId;
        }

        public Builder withName(final Component name)
        {
            this.name = name;
            return this;
        }

        public Builder addPackRequirement(final PackTypeRequirement requirement)
        {
            this.packRequirements.add(requirement);
            return this;
        }

        public Builder addSchematicRequirement(final PackTypeSchematicRequirement requirement)
        {
            this.schematicRequirements.add(requirement);
            return this;
        }

        public PackType build()
        {
            return new PackType(this.packId, this.name, this.packRequirements, this.schematicRequirements);
        }
    }
}
