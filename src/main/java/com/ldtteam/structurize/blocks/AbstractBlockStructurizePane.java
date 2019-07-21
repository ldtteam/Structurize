package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.blocks.interfaces.IBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.PaneBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class AbstractBlockStructurizePane<B extends AbstractBlockStructurizePane<B>> extends PaneBlock implements IBlockStructurize<B>
{
    protected AbstractBlockStructurizePane(final Properties properties)
    {
        super(properties);
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    @Override
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
