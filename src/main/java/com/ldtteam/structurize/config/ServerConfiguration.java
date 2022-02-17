package com.ldtteam.structurize.config;

import com.google.common.collect.Lists;
import com.ldtteam.structurize.update.UpdateMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

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
     * Max amount of blocks checked by a possible worker.
     */
    public final ForgeConfigSpec.IntValue schematicBlockLimit;

    public final ForgeConfigSpec.ConfigValue<String> iteratorType;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sections;

    public final ForgeConfigSpec.EnumValue<UpdateMode> updateMode;

    public final ForgeConfigSpec.ConfigValue<List<Integer>> updateStartPos;

    public final ForgeConfigSpec.ConfigValue<List<Integer>> updateEndPos;

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
        schematicBlockLimit = defineInteger(builder, "schematicBlockLimit", 100000, 1000, 1000000);
        iteratorType = defineString(builder, "iteratorType", "default");
        sections = defineList(builder, "sections", new ArrayList<>(), o -> o instanceof String);

        finishCategory(builder);

        createCategory(builder, "update");

        updateMode = defineEnum(builder, "mode", UpdateMode.DISABLED);
        updateStartPos = builder.define("start", Lists.newArrayList(-10,-10));
        updateEndPos = builder.define("end", Lists.newArrayList(10,10));

        finishCategory(builder);
    }
}