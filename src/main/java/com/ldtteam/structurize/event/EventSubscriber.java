package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Instances;
import com.ldtteam.structurize.command.EntryPoint;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

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
     * Called when server will load a world (client: play button, dedi server: after {@link LifecycleSubscriber()}).
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerAboutToStart(final FMLServerAboutToStartEvent event)
    {
        Instances.getLogger().warn("FMLServerAboutToStartEvent");
    }

    /**
     * Called when world is about to load.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartingEvent event)
    {
        Instances.getLogger().warn("FMLServerStartingEvent");
        EntryPoint.register(event.getCommandDispatcher());
    }

    /**
     * Called when world is loaded.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStarted(final FMLServerStartedEvent event)
    {
        Instances.getLogger().warn("FMLServerStartedEvent");
    }

    /**
     * Called when world is about to stop.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStopping(final FMLServerStoppingEvent event)
    {
        Instances.getLogger().warn("FMLServerStoppingEvent");
    }

    /**
     * Called when world is stopped.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStopped(final FMLServerStoppedEvent event)
    {
        Instances.getLogger().warn("FMLServerStoppedEvent");
    }

    /**
     * Called when config has changed.
     *
     * @param event event
     */
    public static void onConfigChanged(final OnConfigChangedEvent event)
    {
        if (event.getModID().equals(""))
        {

        }
    }
}
