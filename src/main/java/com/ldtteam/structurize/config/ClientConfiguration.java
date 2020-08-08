package com.ldtteam.structurize.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    /**
     * Use Optifine Compat Patch. If false OptifineCompat will never initialize.
     */
    public final ForgeConfigSpec.BooleanValue useOptifineCompatPatch;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "optifine");

        useOptifineCompatPatch = defineBoolean(builder, "useOptifineCompatPatch", true);

        finishCategory(builder);
    }
}
