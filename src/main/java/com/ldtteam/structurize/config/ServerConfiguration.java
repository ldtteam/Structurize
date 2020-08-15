package com.ldtteam.structurize.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /**
     * Should the default schematics be ignored (from the jar)?
     */
    public final ForgeConfigSpec.BooleanValue ignoreSchematicsFromJar;

    /**
     * Should player made schematics be allowed
     */
    public final ForgeConfigSpec.BooleanValue allowPlayerSchematics;

    /**
     * Max world operations per tick (Max blocks to place, remove or replace)
     */
    public final ForgeConfigSpec.IntValue maxOperationsPerTick;

    /**
     * Max amount of changes cached to be able to undo
     */
    public final ForgeConfigSpec.IntValue maxCachedChanges;

    /**
     * Max amount of schematics to be cached on the server
     */
    public final ForgeConfigSpec.IntValue maxCachedSchematics;

    /**
     * Max amount of blocks checked by a possible worker.
     */
    public final ForgeConfigSpec.IntValue maxBlocksChecked;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        ignoreSchematicsFromJar = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        allowPlayerSchematics = defineBoolean(builder, "allowPlayerSchematics", true);
        maxOperationsPerTick = defineInteger(builder, "maxOperationsPerTick", 1000, 0, 100000);
        maxCachedChanges = defineInteger(builder, "maxCachedChanges", 10, 0, 100);
        maxCachedSchematics = defineInteger(builder, "maxCachedSchematics", 100, 0, 100000);
        maxBlocksChecked = defineInteger(builder, "maxBlocksChecked", 1000, 0, 100000);

        finishCategory(builder);
    }
}