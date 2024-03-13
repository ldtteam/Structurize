package com.ldtteam.structurize.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Stores blockState and tileEntity at position.
 */
public record PositionStorage(BlockState getState, @Nullable BlockEntity getEntity)
{
}
