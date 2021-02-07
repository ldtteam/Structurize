package com.ldtteam.structurize.generation.defaults;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.List;

public class CollectionProviderSet
{
    public static void collectionProviderSet(
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

    public static void collectionProviderSet(
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

    public static void each(
      GatherDataEvent event,
      String modId,
      List<IBlockCollection> collections,
      String textureDirectory)
    {
        for (IBlockCollection collection : collections)
        {
            collectionProviderSet(event, modId, collection.getBlocks(), textureDirectory);
        }
    }
}
