package com.ldtteam.structurize;

import com.ldtteam.structurize.network.NetworkChannel;
import com.ldtteam.structurize.util.constants.GeneralConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for storing mod-wide class instances
 * Can be called after {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
 */
public class Instances
{
    private static final NetworkChannel GENERAL_NETWORK_CHANNEL;
    private static final Logger MOD_LOGGER;

    static
    {
        GENERAL_NETWORK_CHANNEL = new NetworkChannel("net-channel");
        MOD_LOGGER = LogManager.getLogger(GeneralConstants.MOD_ID);
    }

    /**
     * Private constructor to hide implicit public one.
     */
    private Instances()
    {
        /**
         * Intentionally left empty
         */
    }

    public static NetworkChannel getNetwork()
    {
        return GENERAL_NETWORK_CHANNEL;
    }

    public static Logger getModLogger()
    {
        return MOD_LOGGER;
    }
}
