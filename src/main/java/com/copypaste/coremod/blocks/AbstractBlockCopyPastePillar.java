package com.copypaste.coremod.blocks;

import com.copypaste.api.util.constant.Suppression;
import com.copypaste.coremod.blocks.interfaces.IBlockCopyPaste;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockCopyPastePillar<B extends AbstractBlockCopyPastePillar<B>> extends BlockRotatedPillar implements IBlockCopyPaste<B>
{
    public AbstractBlockCopyPastePillar(final Material blockMaterialIn)
    {
        super(blockMaterialIn);
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }


    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
    }
}
