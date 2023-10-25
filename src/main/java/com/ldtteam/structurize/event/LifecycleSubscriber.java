package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.datagen.BlockEntityTagProvider;
import com.ldtteam.structurize.datagen.BlockTagProvider;
import com.ldtteam.structurize.datagen.EntityTagProvider;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.NotNull;

public class LifecycleSubscriber
{
    /**
     * Called when mod is being initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onModInit(final FMLCommonSetupEvent event)
    {
        Network.getNetwork().registerCommonMessages();
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
    public static void onDatagen(@NotNull final GatherDataEvent event)
    {
        final DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new BlockEntityTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK_ENTITY_TYPE, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeServer(), new BlockTagProvider(event.getGenerator().getPackOutput(), Registries.BLOCK, event.getLookupProvider(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new EntityTagProvider(event.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, event.getLookupProvider(), event.getExistingFileHelper()));
    }
}
