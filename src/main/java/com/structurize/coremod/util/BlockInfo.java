package com.structurize.coremod.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Contains block information we need for placement.
 */
public class BlockInfo
{
    /**
     * The position in the structure.
     */
    private final BlockPos pos;

    /**
     * The blockstate to be placed.
     */
    @Nullable
    private final IBlockState state;

    /**
     * The tileEntity information we need.
     */
    @Nullable
    private final NBTTagCompound tileEntityData;

    /**
     * Creator of the block info class.
     * @param pos the position.
     * @param state the state.
     * @param tileEntityData the tileEntity data.
     */
    public BlockInfo(final BlockPos pos, @Nullable final IBlockState state, @Nullable final NBTTagCompound tileEntityData)
    {
        this.pos = pos;
        this.state = state;
        this.tileEntityData = tileEntityData;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    @Nullable
    public IBlockState getState()
    {
        return state;
    }

    @Nullable
    public NBTTagCompound getTileEntityData()
    {
        return tileEntityData;
    }
}
