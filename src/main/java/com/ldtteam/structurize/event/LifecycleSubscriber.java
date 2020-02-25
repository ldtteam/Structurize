package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.client.renderer.PlaceholderTileEntityRenderer;
import com.ldtteam.structurize.generation.defaults.DefaultBlockLootTableProvider;
import com.ldtteam.structurize.generation.shingle_slabs.*;
import com.ldtteam.structurize.generation.shingles.*;
import com.ldtteam.structurize.generation.timber_frames.*;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.tileentities.StructurizeTileEntities;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

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
        Log.getLogger().warn("FMLCommonSetupEvent");
        Network.getNetwork().registerCommonMessages();
        StructureLoadingUtils.originFolders.add(Constants.MOD_ID);
    }

    /**
     * Called when client app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        OptifineCompat.getInstance().intialize();
        ModBlocks.getTimberFrames().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getShingles().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getShingleSlabs().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getPaperwalls().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getTranslucent()));
        ClientRegistry.bindTileEntityRenderer(StructurizeTileEntities.PLACERHOLDER_BLOCK, PlaceholderTileEntityRenderer::new);
    }

    /**
     * Called when MC loading is about to finish.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onLoadComplete(final FMLLoadCompleteEvent event)
    {
        Log.getLogger().warn("FMLLoadCompleteEvent");
        LanguageHandler.setMClanguageLoaded();
    }

    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     *
     * @param event event sent when you run the "runData" gradle task
     */
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        // Shingles
        event.getGenerator().addProvider(new ShinglesBlockStateProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesItemModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesBlockModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesLangEntryProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesTagsProvider(event.getGenerator()));

        // Shingle Slabs
        event.getGenerator().addProvider(new ShingleSlabsBlockStateProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsItemModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsBlockModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsLangEntryProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsTagsProvider(event.getGenerator()));

        // Timber Frames
        event.getGenerator().addProvider(new TimberFramesBlockStateProvider(event.getGenerator()));
        event.getGenerator().addProvider(new TimberFramesItemModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new TimberFramesBlockModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new TimberFramesLangEntryProvider(event.getGenerator()));
        event.getGenerator().addProvider(new TimberFramesRecipeProvider(event.getGenerator()));

        // Default
        event.getGenerator().addProvider(new DefaultBlockLootTableProvider(event.getGenerator()));
    }
}
