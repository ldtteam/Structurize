package com.ldtteam.structurize.tileentities;

import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

/**
 * This Class is about the placeholder tileEntity.
 */
public class TileEntityPlaceholder extends TileEntity
{
    /**
     * The block to render.
     */
    private ItemStack block = new ItemStack(ModBlocks.placeholderBlock, 1);

    public TileEntityPlaceholder()
    {
        super(StructurizeTileEntities.PLACERHOLDER_BLOCK);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
        this.block = ItemStack.read(compound.getCompound("displayblock"));
    }

    @NotNull
    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        super.write(compound);
        compound.put("displayblock", this.block.write(new CompoundNBT()));
        return compound;
    }

    @Override
    public void handleUpdateTag(final CompoundNBT tag)
    {
        this.read(tag);
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt)
    {
        this.read(pkt.getNbtCompound());
    }

    @NotNull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return write(new CompoundNBT());
    }

    /**
     * Get the stack of this placerholder.
     * @return the stack.
     */
    public ItemStack getStack()
    {
        return this.block;
    }

    /**
     * Set the stack to the placerholder.
     * @param to the stack to set.
     */
    public void setStack(final ItemStack to)
    {
        this.block = to;
        this.markDirty();
    }
}
