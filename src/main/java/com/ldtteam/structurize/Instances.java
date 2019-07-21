package com.ldtteam.structurize;

import com.ldtteam.structurize.config.Configuration;
import com.ldtteam.structurize.network.NetworkChannel;
import com.ldtteam.structurize.util.constants.GeneralConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Class for storing mod-wide class instances
 * Can be called after {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}
 */
public class Instances
{
    private static final NetworkChannel GENERAL_NETWORK_CHANNEL;
    private static final Logger MOD_LOGGER;
    private static final Configuration MOD_CONFIG;

    static
    {
        GENERAL_NETWORK_CHANNEL = new NetworkChannel("net-channel");
        MOD_LOGGER = LogManager.getLogger(GeneralConstants.MOD_ID);
        MOD_CONFIG = new Configuration(ModLoadingContext.get().getActiveContainer());
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

    public static Logger getLogger()
    {
        return MOD_LOGGER;
    }

    public static Configuration getConfig()
    {
        return MOD_CONFIG;
    }
}
