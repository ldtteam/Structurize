package com.ldtteam.structurize.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;

import java.util.Arrays;
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
public interface IBlockList<B extends Block>
{
    String getName();

    RegistryObject<B> getBlock();

    static <B extends Block> List<RegistryObject<B>> asList(IBlockList<B>[] values)
    {
        return Arrays.stream(values).map(IBlockList::getBlock).collect(Collectors.toList());
    }
}
