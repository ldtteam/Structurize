package com.ldtteam.structurize.api.util.constant;

/**
 * Some constants needed to store things to NBT.
 */
public final class NbtTagConstants
{
    public static final String TAG_UUID                   = "uuid";

    /**
     * Var for first pos string.
     */
    public static final String FIRST_POS_STRING = "structurize:start_pos";

    /**
     * Var for second pos string.
     */
    public static final String SECOND_POS_STRING = "structurize:end_pos";

    /**
     * NBT tag constants for MultiBlock tileEntities.
     */
    public static final String TAG_INPUT            = "input";
    public static final String TAG_RANGE            = "range";
    public static final String TAG_DIRECTION        = "direction";
    public static final String TAG_LENGTH           = "length";
    public static final String TAG_PROGRESS         = "progress";
    public static final String TAG_OUTPUT_DIRECTION = "outputDirection";
    public static final String TAG_SPEED            = "speed";

    /**
     * Private constructor to hide the implicit one.
     */
    private NbtTagConstants()
    {
        /*
         * Intentionally left empty.
         */
    }
}
