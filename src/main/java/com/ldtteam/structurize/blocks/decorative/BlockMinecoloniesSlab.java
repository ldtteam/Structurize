package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.api.util.constant.Suppression;
import com.ldtteam.structurize.blocks.interfaces.IBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public class BlockMinecoloniesSlab<B extends BlockMinecoloniesSlab<B>> extends SlabBlock implements IBlockStructurize<B>
{
    /**
     * Constructor of abstract class.
     * @param properties the input properties.
     */
    public BlockMinecoloniesSlab(final Properties properties, final String registryName)
    {
        super(properties);
        this.setRegistryName(registryName);
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
