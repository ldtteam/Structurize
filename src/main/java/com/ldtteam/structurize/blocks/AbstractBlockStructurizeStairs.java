package com.ldtteam.structurize.blocks;

import java.util.function.Supplier;
import com.ldtteam.structurize.api.util.constant.Suppression;
import com.ldtteam.structurize.blocks.interfaces.IBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockStructurizeStairs<B extends AbstractBlockStructurizeStairs<B>> extends StairsBlock implements IBlockStructurize<B>
{
    public AbstractBlockStructurizeStairs(final Supplier<BlockState> state, final Properties properties)
    {
        super(state, properties);
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
