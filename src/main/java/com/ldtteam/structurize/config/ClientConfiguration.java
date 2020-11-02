package com.ldtteam.structurize.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    /**
     * Excluded entities.
     */
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> excludedEntities;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        excludedEntities = defineList(builder, "excludeEntities", Arrays.asList("minecraft:iron_golem", "minecraft:wolf"), s -> s instanceof String);
    }
}