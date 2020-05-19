package com.ldtteam.structurize.util;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Contains block information we need for placement.
 */
public class BlockInfo
{
    /**
     * The position in the structure (relative pos not world pos).
     */
    private final BlockPos pos;

    /**
     * The blockstate to be placed.
     */
    @Nullable
    private final BlockState state;

    /**
     * The tileEntity information we need.
     */
    @Nullable
    private final CompoundNBT tileEntityData;

    /**
     * Creator of the block info class.
     *
     * @param pos            the position.
     * @param state          the state.
     * @param tileEntityData the tileEntity data.
     */
    public BlockInfo(final BlockPos pos, @Nullable final BlockState state, @Nullable final CompoundNBT tileEntityData)
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
    public BlockState getState()
    {
        return state;
    }

    @Nullable
    public CompoundNBT getTileEntityData()
    {
        return tileEntityData;
    }

    public boolean hasTileEntityData()
    {
        return tileEntityData != null;
    }
}
