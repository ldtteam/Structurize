package com.ldtteam.structurize;

import com.ldtteam.structures.event.RenderEventHandler;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.config.Configuration;
import com.ldtteam.structurize.event.*;
import com.ldtteam.structurize.proxy.ClientProxy;
import com.ldtteam.structurize.proxy.IProxy;
import com.ldtteam.structurize.proxy.ServerProxy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod main class.
 * The value in annotation should match an entry in the META-INF/mods.toml file.
 */
@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Structurize
{
    /**
     * The proxy.
     */
    public static final IProxy proxy = DistExecutor.runForDist( () -> ClientProxy::new, () -> ServerProxy::new);

    /**
     * Our mod logger.
     */
    private static final Logger logger = LogManager.getLogger(Constants.MOD_ID);

    /**
     * The config instance.
     */
    private static final Configuration config = new Configuration(ModLoadingContext.get().getActiveContainer());

    /**
     * Mod init, registers events to their respective busses
     */
    public Structurize()
    {
        logger.warn("Structurize starting up");
        Mod.EventBusSubscriber.Bus.MOD.bus().get().register(LifecycleSubscriber.class);
        Mod.EventBusSubscriber.Bus.MOD.bus().get().addListener(GatherDataHandler::dataGeneratorSetup);
      
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(EventSubscriber.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ClientEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(FMLEventHandler.class);
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(RenderEventHandler.class);
    }

    /**
     * Getter for the structurize Logger.
     *
     * @return the logger.
     */
    public static Logger getLogger()
    {
        return logger;
    }

    /**
     * Get the config handler.
     * @return the config handler.
     */
    public static Configuration getConfig()
    {
        return config;
    }
}
