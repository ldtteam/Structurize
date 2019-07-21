package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Class with methods for receiving various forge events.
 * Methods are sorted according to time of execution.
 */
public class LifecycleSubscriber
{
    /**
     * Private constructor to hide implicit public one.
     */
    private LifecycleSubscriber()
    {
        /**
         * Intentionally left empty
         */
    }

    /**
     * Called when mod is being initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onModInit(final FMLCommonSetupEvent event)
    {
        Structurize.getLogger().warn("FMLCommonSetupEvent");
        Structurize.getNetwork().registerCommonMessages();
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        Structurize.getLogger().warn("FMLLoadCompleteEvent");
        LanguageHandler.setMClanguageLoaded();
    }
}
