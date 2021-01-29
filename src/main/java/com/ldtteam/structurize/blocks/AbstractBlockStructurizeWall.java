package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.util.constant.Suppression;
import com.ldtteam.structurize.blocks.interfaces.IBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * The abstract class for structurize-added walls.
 * Currently only used by brown brick, beige brick,
 * and cream brick walls.
 */

public abstract class AbstractBlockStructurizeWall<B extends AbstractBlockStructurizeWall<B>> extends WallBlock implements IBlockStructurize<B>
        /**
         * Extend the vanilla WallBlock
         */
{
    public AbstractBlockStructurizeWall(final Block.Properties properties)
    {
        /**
         * Lets you set the block properties
         */
        super(properties);
    }

    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }

    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register((new BlockItem(this, properties)).setRegistryName(this.getRegistryName()));
    }
}
