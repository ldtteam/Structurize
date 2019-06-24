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
    private static final Instances INSTANCE = new Instances();

    private final NetworkChannel generalNetworkChannel;
    private final Logger modLogger;

    private Instances()
    {
        generalNetworkChannel = new NetworkChannel("net-channel");
        modLogger = LogManager.getLogger(GeneralConstants.MOD_ID);
    }

    public static NetworkChannel getNetwork()
    {
        return INSTANCE.generalNetworkChannel;
    }

    public static Logger getModLogger()
    {
        return INSTANCE.modLogger;
    }
}