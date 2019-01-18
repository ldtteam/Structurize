package com.structurize.api.util.constant;

import static com.structurize.api.util.constant.Constants.MOD_ID;

/**
 * Some constants needed to store things to NBT.
 */
public final class NbtTagConstants
{
    public static final String TAG_UUID                   = "uuid";

    /**
     * Var for first pos string.
     */
    public static final String FIRST_POS_STRING = "pos1";

    /**
     * Var for second pos string.
     */
    public static final String SECOND_POS_STRING = "pos2";

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
     * Ids and names for the base scanning steps in structurize.
     */
    public static final String TAG_NAME_STEP = MOD_ID + ":name";
    public static final String TAG_POSITION_STEP = MOD_ID + ":position";
    public static final String TAG_REPLACE_VALUES_STEP = MOD_ID + ":replace";

    /**
     * Tag Data relevant for naming step
     */
    public static final String TAG_NAME_STEP_NAME = "name";

    /**
     * Tag Data relevant for the position step
     */
    public static final String TAG_POSITION_STEP_FROM = "from";
    public static final String TAG_POSITION_STEP_TO = "to";
    public static final String TAG_POSITION_STEP_DIM = "dim";


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
