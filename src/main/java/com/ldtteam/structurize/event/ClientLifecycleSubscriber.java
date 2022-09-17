package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.network.messages.AdvanceQueueMessage;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyMappingLookup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.mojang.blaze3d.platform.InputConstants.KEY_B;

public class ClientLifecycleSubscriber
{
    public static final KeyMapping ADVANCE_QUEUE = new KeyMapping("Advance Queue",
      KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM.getOrCreate(KEY_B), "structurize");

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
                protected Object prepare(final ResourceManager manager, final ProfilerFiller profiler)
                {
                    return new Object();
                }

                @Override
                protected void apply(final Object source, final ResourceManager manager, final ProfilerFiller profiler)
                {
                    Log.getLogger().debug("Clearing blueprint renderer cache.");
                    BlueprintHandler.getInstance().clearCache();
                }
            });
        }

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(ClientLifecycleSubscriber::handleKeyInputEvent);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(ADVANCE_QUEUE);
    }

    public static void handleKeyInputEvent(TickEvent.ClientTickEvent event)
    {
        if (ADVANCE_QUEUE.consumeClick()) {
            Network.getNetwork().sendToServer(new AdvanceQueueMessage());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void doClientStuff(final EntityRenderersEvent.RegisterRenderers event)
    {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.blockSubstitution.get(), RenderType.translucent());
    }
}
