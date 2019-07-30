package com.ldtteam.structurize.event;

import com.ldtteam.structurize.generation.defaults.DefaultBlockLootTableProvider;
import com.ldtteam.structurize.generation.shingle_slabs.*;
import com.ldtteam.structurize.generation.shingles.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

/**
 * EventHandler used to generate data during the runData gradle task.
 */
@Mod.EventBusSubscriber
public class GatherDataHandler
{
    /**
     * This method is for adding datagenerators. this does not run during normal client operations, only during building.
     * @param event event sent when you run the "runData" gradle task
     */
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        //Shingles
        event.getGenerator().addProvider(new ShinglesBlockStateProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesTagsProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesLangEntryProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesItemModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShinglesBlockModelProvider(event.getGenerator()));

        //Shingle Slabs
        event.getGenerator().addProvider(new ShingleSlabsBlockStateProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsRecipeProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsTagsProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsLangEntryProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsItemModelProvider(event.getGenerator()));
        event.getGenerator().addProvider(new ShingleSlabsBlockModelProvider(event.getGenerator()));

        //Default
        event.getGenerator().addProvider(new DefaultBlockLootTableProvider(event.getGenerator()));
    }
}
