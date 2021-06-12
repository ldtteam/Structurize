package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.generation.*;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.commands.arguments.MultipleStringArgument;
import com.ldtteam.structurize.generation.DefaultBlockLootTableProvider;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        Network.getNetwork().registerCommonMessages();
        ArgumentTypes.register(Constants.MOD_ID + ":multistring", MultipleStringArgument.class, new MultipleStringArgument.Serializer());
        StructureLoadingUtils.addOriginMod(Constants.MOD_ID);
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
        event.getGenerator().addProvider(new ModLanguageProvider(event.getGenerator(), Constants.MOD_ID, "default"));
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
