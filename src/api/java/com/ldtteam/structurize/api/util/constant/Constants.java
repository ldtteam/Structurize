package com.ldtteam.structurize.api.util.constant;

import net.minecraft.world.level.block.Block;

/**
 * Some constants needed for the whole mod.
 */
public final class Constants
{
    public static final String MOD_ID                  = "structurize";
    public static final String MOD_NAME                = "Structurize";
    public static final int    TICKS_SECOND            = 20;
    public static final int    SECONDS_A_MINUTE        = 60;
    public static final int    UPDATE_FLAG             = Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS;
    public static final String MINECOLONIES_MOD_ID     = "minecolonies";
    public static final String GROUNDLEVEL_TAG         = "groundlevel";
    public static final String INVISIBLE_TAG           = "invisible";
    public static final int    GROUNDSTYLE_RELATIVE    = 1; // relative to anchor
    public static final int    GROUNDSTYLE_LEGACY_CAMP = 2; // 1 block at bottom
    public static final int    GROUNDSTYLE_LEGACY_SHIP = 3; // 3 blocks at bottom
    public static final String BLUEPRINT_FOLDER        = "blueprints";
    public static final String UPDATE_FOLDER           = "updater";

    public static final String CLIENT_FOLDER           = "clients";
    public static final String SCANS_FOLDER            = "scans";
    public static final String SHAPES_FOLDER           = "shapes";

    /**
     * Size of the buffer.
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Local standard.
     */
    public static final String LOCAL = "Local";

    /**
     * Private constructor to hide implicit public one.
     */
    private Constants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
