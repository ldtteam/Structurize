package com.ldtteam.structurize.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Contains block information we need for placement.
 */
public record BlockInfo(BlockPos getPos, @Nullable BlockState getState, @Nullable CompoundTag getTileEntityData)
{
    public boolean hasTileEntityData()
    {
        return getTileEntityData != null;
    }
}
