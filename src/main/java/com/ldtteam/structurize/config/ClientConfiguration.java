package com.ldtteam.structurize.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    /**
     * How many parsed GUI windows to keep track of in the cache
     */
    public final ForgeConfigSpec.IntValue windowCacheCap;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        windowCacheCap = defineInteger(builder, "windowCacheCap", 12, 0, 100);
    }
}