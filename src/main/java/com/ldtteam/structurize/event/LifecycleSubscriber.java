package com.ldtteam.structurize.event;

import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.generation.*;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.generation.DefaultBlockLootTableProvider;
import com.ldtteam.structurize.generation.StructurizeTranslations;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import java.util.function.Predicate;

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
        StructureLoadingUtils.addOriginMod(Constants.MOD_ID);
    }

    /**
     * Called when client app is initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onClientInit(final FMLClientSetupEvent event)
    {
        ModBlocks.getTimberFrames().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getShingles().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getShingleSlabs().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        ModBlocks.getPaperWalls().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getTranslucent()));
        ModBlocks.getFloatingCarpets().forEach(frame -> RenderTypeLookup.setRenderLayer(frame, RenderType.getCutout()));
        OptifineCompat.getInstance().intialize();

        final IResourceManager rm = event.getMinecraftSupplier().get().getResourceManager();
        if (rm instanceof IReloadableResourceManager)
        {
            ((IReloadableResourceManager) rm).addReloadListener(new ISelectiveResourceReloadListener()
            {
                @Override
                public void onResourceManagerReload(final IResourceManager resourceManager,
                    final Predicate<IResourceType> resourcePredicate)
                {
                    if (resourcePredicate.test(VanillaResourceType.MODELS) || resourcePredicate.test(VanillaResourceType.TEXTURES)
                        || resourcePredicate.test(VanillaResourceType.SHADERS))
                    {
                        Log.getLogger().debug("Clearing blueprint renderer cache.");
                        BlueprintHandler.getInstance().clearCache();
                    }
                }
            });
        }
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

    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     *
     * @param event event sent when you run the "runData" gradle task
     */
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        // Initialise All Singletons
        event.getGenerator().addProvider(new StructurizeTranslations(event.getGenerator(), Constants.MOD_ID, "en_us"));
        event.getGenerator().addProvider(new ModRecipeProvider(event.getGenerator()));
        ModBlockTagsProvider mbt = new ModBlockTagsProvider(event.getGenerator(), Constants.MOD_ID, event.getExistingFileHelper());
        event.getGenerator().addProvider(mbt);
        event.getGenerator().addProvider(new ModItemTagsProvider(event.getGenerator(), mbt, Constants.MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new ModBlockStateProvider(event.getGenerator(), Constants.MOD_ID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new ModItemModelProvider(event.getGenerator(), Constants.MOD_ID, event.getExistingFileHelper()));

        ModBlocks.BRICKS.forEach(type -> type.provide(event));
        ModBlocks.CACTI_BLOCKS.provide(event);
        ModBlocks.timberFrames.forEach(type -> type.provide(event));
        ModBlocks.shingles.forEach(type -> type.provide(event));
        ModBlocks.floatingCarpets.provide(event);
        ModBlocks.shingleSlabs.provide(event);
        ModBlocks.paperWalls.provide(event);

        // Default
        event.getGenerator().addProvider(new DefaultBlockLootTableProvider(event.getGenerator()));

    }
}
