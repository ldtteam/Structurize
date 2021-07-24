package com.ldtteam.structurize.api.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Stores blockState and tileEntity at position.
 */
public class PositionStorage
{
    /**
     * The state at a position.
     */
    private final BlockState state;

    /**
     * The tileEntity at a position.
     */
    @Nullable
    private final BlockEntity entity;

    /**
     * Constructor for the storage.
     * @param state the state.
     * @param entity the entity.
     */
    public PositionStorage(final BlockState state, @Nullable final BlockEntity entity)
    {
        this.state = state;
        this.entity = entity;
    }

    /**
     * Get the state of the storage.
     * @return the BlockState.
     */
    public BlockState getState()
    {
        return state;
    }

    /**
     * Get the entity of the storage.
     * @return the TileEntity.
     */
    @Nullable
    public BlockEntity getEntity()
    {
        return entity;
    }
}
