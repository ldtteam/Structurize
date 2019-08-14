package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Structures;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

/**
 * Class with methods for receiving various forge events
 */
public class EventSubscriber
{
    /**
     * Private constructor to hide implicit public one.
     */
    private EventSubscriber()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Called when world is about to load.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartingEvent event)
    {
        Log.getLogger().warn("FMLServerStartingEvent");
        EntryPoint.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartedEvent event)
    {
        Log.getLogger().warn("FMLServerStartedEvent");
        Structures.init();
    }
}
