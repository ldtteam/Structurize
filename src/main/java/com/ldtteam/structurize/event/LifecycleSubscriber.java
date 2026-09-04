package com.ldtteam.structurize.event;

import com.ldtteam.structurize.datagen.BlockEntityTagProvider;
import com.ldtteam.structurize.datagen.BlockTagProvider;
import com.ldtteam.structurize.datagen.EntityTagProvider;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

public class LifecycleSubscriber
{
    @SubscribeEvent
    public static void onRegisterPayloads(final RegisterPayloadHandlersEvent event)
    {
        final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).versioned(modVersion);
        Network.getNetwork().registerCommonMessages(registrar);
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        LanguageHandler.setMClanguageLoaded();
    }

    @SubscribeEvent
    public static void onServerDatagen(@NotNull final GatherDataEvent.Server event)
    {
        final DataGenerator generator = event.getGenerator();
        event.addProvider(new BlockEntityTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK_ENTITY_TYPE, event.getLookupProvider()));
        event.addProvider(new BlockTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK, event.getLookupProvider()));
    }

    @SubscribeEvent
    public static void onClientDatagen(@NotNull final GatherDataEvent.Client event)
    {
        final DataGenerator generator = event.getGenerator();
        if (event instanceof GatherDataEvent.Client)
        {
            event.addProvider(new EntityTagProvider(event.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, event.getLookupProvider()));
        }
    }
}
