package com.ldtteam.structurize.generation.defaults;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.List;

public class CollectionProviderSet
{
    public CollectionProviderSet(
      GatherDataEvent event,
      String modId,
      List<Block> collection,
      IItemProvider material,
      String texture)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper filer = event.getExistingFileHelper();
        gen.addProvider(new CollectionBlockStateProvider(gen, modId, filer, collection, texture));
        gen.addProvider(new CollectionRecipeProvider(gen, collection, material));
        gen.addProvider(new CollectionLanguageProvider(gen, modId, collection));
    }

    public CollectionProviderSet(
      GatherDataEvent event,
      String modId,
      List<Block> collection,
      String texture)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper filer = event.getExistingFileHelper();
        gen.addProvider(new CollectionBlockStateProvider(gen, modId, filer, collection, texture));
        gen.addProvider(new CollectionRecipeProvider(gen, collection));
        gen.addProvider(new CollectionLanguageProvider(gen, modId, collection));
    }

    public static void each(List<List<Block>> collections)
    {
        //for (List<Block> collection : collections) new CollectionProviderSet();
    }
}
