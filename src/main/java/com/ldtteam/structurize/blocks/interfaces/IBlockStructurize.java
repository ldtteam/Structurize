package com.ldtteam.structurize.blocks.interfaces;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public interface IBlockStructurize<B extends IBlockStructurize<B>>
{
    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    B registerBlock(final IForgeRegistry<Block> registry);

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    void registerItemBlock(final IForgeRegistry<Item> registry);
}
