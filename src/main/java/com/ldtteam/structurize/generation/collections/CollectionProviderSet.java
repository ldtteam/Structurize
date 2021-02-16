package com.ldtteam.structurize.generation.collections;

import com.ldtteam.structurize.blocks.BlockType;
import com.ldtteam.structurize.blocks.IBlockCollection;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.generation.ModBlockTagsProvider;
import com.ldtteam.structurize.generation.ModLanguageProvider;
import com.ldtteam.structurize.generation.ModRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.List;

/**
 * A utility class to create providers for collections.
 * Preferably collection providers should not be sold separately
 */
public class CollectionProviderSet
{
    public static void collectionProviderSet(
      GatherDataEvent event,
      String modId,
      IBlockCollection collection,
      IItemProvider material,
      String textureDirectory)
    {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper filer = event.getExistingFileHelper();
        gen.addProvider(new CollectionBlockStateProvider(gen, modId, filer, collection, textureDirectory));
        gen.addProvider(new CollectionItemModelProvider(gen, modId, filer, collection, textureDirectory));

        // -- Recipes -- //
        collection.getBlocks().forEach(ro -> ModRecipeProvider.getInstance()
          .add(consumer -> {
              if (ro.get() == collection.getMainBlock())
              {
                  collection.provideMainRecipe(consumer, ModRecipeProvider.getDefaultCriterion(collection.getMainBlock()));
                  return;
              }
              BlockType.fromSuffix(ro.get())
                .formRecipe(ro.get(), collection.getMainBlock(), ModRecipeProvider.getDefaultCriterion(collection.getMainBlock()))
                .build(consumer);
          }));

        // -- Block Tags -- // (Item Tags are handled in the mod-wide provider)
        collection.getBlocks().forEach(
          ro -> BlockType.fromSuffix(ro.get()).blockTag.forEach(
            tag -> ModBlockTagsProvider.getInstance()
              .buildTag(tag)
              .add(ro.get())));

        // -- Language -- //
        ModLanguageProvider.getInstance().autoTranslate(ModBlocks.getList(collection.getBlocks()));

    }

    public static void each(
      GatherDataEvent event,
      String modId,
      List<IBlockCollection> collections,
      String textureDirectory)
    {
        for (IBlockCollection collection : collections)
        {
            collectionProviderSet(event, modId, collection, textureDirectory);
        }
    }
}
