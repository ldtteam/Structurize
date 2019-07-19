package com.ldtteam.structurize.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeBlockState;
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
    private final IForgeBlockState state;

    /**
     * The tileEntity information we need.
     */
    @Nullable
    private final CompoundNBT tileEntityData;

    /**
     * Creator of the block info class.
     * @param pos the position.
     * @param state the state.
     * @param tileEntityData the tileEntity data.
     */
    public BlockInfo(final BlockPos pos, @Nullable final IForgeBlockState state, @Nullable final CompoundNBT tileEntityData)
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
    public IForgeBlockState getState()
    {
        return state;
    }

    @Nullable
    public CompoundNBT getTileEntityData()
    {
        return tileEntityData;
    }
}
