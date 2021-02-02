package com.ldtteam.structurize.config;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
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
        excludedEntities = defineList(builder, "excludeEntities", new ArrayList<>(), s -> s instanceof String && ResourceLocation.tryCreate((String) s) != null);
        windowCacheCap = defineInteger(builder, "windowCacheCap", 12, 0, 100);
    }
}