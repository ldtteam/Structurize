package com.ldtteam.structurize.api.blocks;

import com.ldtteam.structurize.api.generation.ModItemModelProvider;
import com.ldtteam.structurize.api.generation.ModLanguageProvider;
import net.minecraft.block.Block;
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
     * A convenience method to get the registry path for this block
     * @param block the block
     * @return the registry key
     */
    default String getRegistryPath(B block)
    {
        return block.getRegistryName() != null? block.getRegistryName().getPath() : "";
    }

    List<RegistryObject<B>> getRegisteredBlocks();

    default List<B> getBlocks()
    {
        return getRegisteredBlocks().stream().map(RegistryObject::get).collect(Collectors.toList());
    }

    @Override
    default void generateItemModels(ModItemModelProvider models)
    {
        getBlocks().forEach(
          block -> models.getBuilder(block.getRegistryName().getPath())
            .parent(new ModelFile.UncheckedModelFile(models.modLoc("block/" + block.getRegistryName().getPath())))
        );
    }

    @Override
    default void generateTranslations(ModLanguageProvider lang)
    {
        lang.autoTranslate(getBlocks());
    }
}
