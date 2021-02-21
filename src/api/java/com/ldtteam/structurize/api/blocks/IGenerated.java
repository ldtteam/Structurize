package com.ldtteam.structurize.api.blocks;

import com.ldtteam.structurize.api.generation.*;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

/**
 * Defines a block or set of blocks that can be generated
 * by calling the provide method in the data gen life cycle
 */
public interface IGenerated
{
    default String getTextureDirectory()
    {
        return "block/";
    }

    void generateBlockStates(ModBlockStateProvider states);

    void generateItemModels(ModItemModelProvider models);

    void generateRecipes(ModRecipeProvider provider);

    void generateTags(ModBlockTagsProvider blocks, ModItemTagsProvider items);

    void generateTranslations(ModLanguageProvider lang);

    /**
     * Provides all necessary data to generate the block(s).
     * This method should be called in the data gen life cycle.
     * @param event the data generator event instance
     */
    default void provide(GatherDataEvent event)
    {
        generateBlockStates(ModBlockStateProvider.getInstance());
        generateItemModels(ModItemModelProvider.getInstance());
        generateRecipes(ModRecipeProvider.getInstance());
        generateTags(ModBlockTagsProvider.getInstance(), ModItemTagsProvider.getInstance());
        generateTranslations(ModLanguageProvider.getInstance());
    }

    static void provide(GatherDataEvent event, IGenerated[] values)
    {
        for (final IGenerated value : values)
        {
            value.provide(event);
        }
    }
}
