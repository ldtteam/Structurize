package com.structurize.api.util.constant;

/**
 * Some constants needed for the whole mod.
 */
public final class Constants
{
    public static final String MOD_ID                           = "structurize";
    public static final String MOD_NAME                         = "Structurize";
    public static final String VERSION                          = "@VERSION@";
    public static final String MC_VERSION                       = "[1.12,1.13]";
    public static final String CLIENT_PROXY_LOCATION            = "com.structurize.coremod.proxy.ClientProxy";
    public static final String SERVER_PROXY_LOCATION            = "com.structurize.coremod.proxy.ServerProxy";
    public static final int    ROTATE_ONCE                      = 1;
    public static final int    ROTATE_TWICE                     = 2;
    public static final int    ROTATE_THREE_TIMES               = 3;
    public static final int    TICKS_SECOND                     = 20;
    public static final int    SECONDS_A_MINUTE                 = 60;
    public static final int    UPDATE_FLAG                      = 0x03;
    public static final double HALF_BLOCK                       = 0.5D;
    public static final String MINECOLONIES_MOD_ID              = "minecolonies";

    /**
     * Volume to play at.
     */
    public static final double VOLUME = 0.5D;

    /**
     * The base pitch, add more to this to change the sound.
     */
    public static final double PITCH = 0.8D;

    /**
     * Maximum message size from client to server (Leaving some extra space).
     */
    public static final int MAX_MESSAGE_SIZE = 30_000;

    /**
     * Maximum amount of pieces from client to server (Leaving some extra space).
     */
    public static final int MAX_AMOUNT_OF_PIECES = 20;

    /**
     * Max schematic size to create.
     */
    public static final int MAX_SCHEMATIC_SIZE = 100_000;

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
