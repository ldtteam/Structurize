package com.ldtteam.structurize.event;

import com.ldtteam.blockout.Loader;
import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.optifine.OptifineCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Predicate;

public class ClientLifecycleSubscriber
{
    /**
     * Called when client app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        OptifineCompat.getInstance().intialize();

        final ResourceManager rm = Minecraft.getInstance().getResourceManager();
        if (rm instanceof final ReloadableResourceManager resourceManager)
        {
            resourceManager.registerReloadListener(new SimplePreparableReloadListener<>()
            {

                @Override
                protected @NotNull Object prepare(final @NotNull ResourceManager manager, final @NotNull ProfilerFiller profiler)
                {
                    return new Object();
                }

                @Override
                protected void apply(final @NotNull Object source, final @NotNull ResourceManager manager, final @NotNull ProfilerFiller profiler)
                {
                    Log.getLogger().debug("Clearing blueprint renderer cache.");
                    BlueprintHandler.getInstance().clearCache();
                    Log.getLogger().debug("Clearing gui XML cache.");
                    Loader.cleanParsedCache();
                }
            });
        }
    }
}
