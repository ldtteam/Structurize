package com.ldtteam.structurize.storage;

import net.minecraft.Util;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.Executors;

public class StructurePackLoader
{
    @SubscribeEvent
    public static void onServerStarted(final ServerStartingEvent event)
    {
        Util.backgroundExecutor().execute(() ->
        {
            //        event.getServer().getServerDirectory()
            //
        });
    }
}
