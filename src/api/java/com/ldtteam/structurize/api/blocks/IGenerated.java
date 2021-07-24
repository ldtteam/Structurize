package com.ldtteam.structurize.api.blocks;

import com.ldtteam.structurize.api.generation.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

/**
 * Defines a block or set of blocks that can be generated
 * by calling the provide method in the data gen life cycle
 */
public interface IGenerated
{
    /**
     * A convenience method to get the registry path for this block,
     * and to stop a ton of unnecessary warnings
     * @param block the block
     * @return the registry key
     */
    default String getRegistryPath(Block block)
    {
        return block.getRegistryName() != null? block.getRegistryName().getPath() : "";
    }

    /**
     * A default place to look for textures. Override to change directory
     * @return the directory path relative to 'textures/' ending with a slash
     */
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

    /**
     * Convenience method to provide many generated sets at once
     * @param event the life cycle event
     * @param values the list of sets or blocks to generate
     */
    static void provide(GatherDataEvent event, IGenerated[] values)
    {
        for (final IGenerated value : values)
        {
            value.provide(event);
        }
    }
}
