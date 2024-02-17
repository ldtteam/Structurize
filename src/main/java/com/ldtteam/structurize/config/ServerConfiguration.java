package com.ldtteam.structurize.config;

import com.ldtteam.common.config.AbstractConfiguration;
import com.ldtteam.structurize.api.constants.Constants;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /**
     * Should the default schematics be ignored (from the jar)?
     */
    public final BooleanValue ignoreSchematicsFromJar;

    /**
     * Should player made schematics be allowed
     */
    public final BooleanValue allowPlayerSchematics;

    /**
     * Max world operations per tick (Max blocks to place, remove or replace)
     */
    public final IntValue maxOperationsPerTick;

    /**
     * Max amount of changes cached to be able to undo
     */
    public final IntValue maxCachedChanges;

    /**
     * Max amount of schematics to be cached on the server
     */
    public final IntValue maxCachedSchematics;

    /**
     * Max amount of blocks checked by a possible worker.
     */
    public final IntValue maxBlocksChecked;

    /**
     * Max amount of blocks checked by a possible worker.
     */
    public final IntValue schematicBlockLimit;

    public final ConfigValue<String> iteratorType;

    public final BooleanValue teleportAllowed;
    public final EnumValue<Direction> teleportBuildDirection;
    public final IntValue teleportBuildDistance;
    public final BooleanValue teleportSafety;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    public ServerConfiguration(final Builder builder)
    {
        super(builder, Constants.MOD_ID);

        createCategory("gameplay");

        ignoreSchematicsFromJar = defineBoolean("ignoreSchematicsFromJar", false);
        allowPlayerSchematics = defineBoolean("allowPlayerSchematics", true);
        maxOperationsPerTick = defineInteger("maxOperationsPerTick", 1000, 0, 100000);
        maxCachedChanges = defineInteger("maxCachedChanges", 50, 0, 250);
        maxCachedSchematics = defineInteger("maxCachedSchematics", 100, 0, 100000);
        maxBlocksChecked = defineInteger("maxBlocksChecked", 1000, 0, 100000);
        schematicBlockLimit = defineInteger("schematicBlockLimit", 100000, 1000, 1000000);
        iteratorType = defineString("iteratorType", "default");

        swapToCategory("teleport");

        teleportAllowed = defineBoolean("teleportAllowed", true);
        teleportBuildDirection = defineEnum("teleportBuildDirection", Direction.SOUTH);
        teleportBuildDistance = defineInteger("teleportBuildDistance", 3, 1, 16);
        teleportSafety = defineBoolean("teleportSafety", true);

        finishCategory();
    }
}