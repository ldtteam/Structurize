package com.ldtteam.structurize.api.util.constant;

import net.minecraftforge.common.util.Constants.BlockFlags;

/**
 * Some constants needed for the whole mod.
 */
public final class Constants
{
    public static final String MOD_ID                           = "structurize";
    public static final String MOD_NAME                         = "Structurize";
    public static final int    ROTATE_ONCE                      = 1;
    public static final int    ROTATE_TWICE                     = 2;
    public static final int    ROTATE_THREE_TIMES               = 3;
    public static final int    TICKS_SECOND                     = 20;
    public static final int    SECONDS_A_MINUTE                 = 60;
    public static final int    UPDATE_FLAG                      = BlockFlags.NOTIFY_NEIGHBORS | BlockFlags.BLOCK_UPDATE;
    public static final String MINECOLONIES_MOD_ID              = "minecolonies";
    public static final String GROUNDLEVEL_TAG                  = "groundlevel";

    /**
     * Maximum message size from client to server (Leaving some extra space).
     */
    public static final int MAX_MESSAGE_SIZE = 30_000;

    /**
     * Maximum amount of pieces from client to server (Leaving some extra space).
     */
    public static final int MAX_AMOUNT_OF_PIECES = 20;

    /**
     * Rotation by 90°.
     */
    public static final double NINETY_DEGREES = 90D;

    /**
     * Size of the buffer.
     */
    public static final int BUFFER_SIZE = 1024;

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
