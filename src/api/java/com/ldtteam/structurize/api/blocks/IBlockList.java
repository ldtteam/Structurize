package com.ldtteam.structurize.api.blocks;

import com.ldtteam.structurize.api.generation.ModItemModelProvider;
import com.ldtteam.structurize.api.generation.ModLanguageProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A list of blocks that are all of the same type.
 * Useful for manipulating block enums.
 *
 * When implementing, blocks should be registered during construction
 *
 * @param <B> the block type this list consists of
 */
public interface IBlockList<B extends Block> extends IGenerated
{
    /**
     * Provides a list of blocks associated with the list for use in various methods
     * @return the list as gettable registry object entries
     */
    List<RegistryObject<B>> getRegisteredBlocks();

    /**
     * A convenience method to fetch the blocks from their registry entries
     * @return the raw list of blocks, freshly retrieved
     */
    default List<B> getBlocks()
    {
        return getRegisteredBlocks().stream().map(RegistryObject::get).collect(Collectors.toList());
    }

    @Override
    default void generateItemModels(ModItemModelProvider models)
    {
        getBlocks().forEach(
          block -> models.getBuilder(getRegistryPath(block))
            .parent(new ModelFile.UncheckedModelFile(models.modLoc("block/" + getRegistryPath(block))))
        );
    }

    @Override
    default void generateTranslations(ModLanguageProvider lang)
    {
        lang.autoTranslate(getBlocks());
    }
}
