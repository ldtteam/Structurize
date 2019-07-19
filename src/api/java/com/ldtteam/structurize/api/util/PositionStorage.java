package com.ldtteam.structurize.api.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stores blockState and tileEntity at position.
 */
public class PositionStorage
{
    /**
     * The state at a position.
     */
    private final IForgeBlockState state;

    /**
     * The tileEntity at a position.
     */
    @Nullable
    private final TileEntity entity;

    /**
     * Constructor for the storage.
     * @param state the state.
     * @param entity the entity.
     */
    public PositionStorage(final IForgeBlockState state, @Nullable final TileEntity entity)
    {
        this.state = state;
        this.entity = entity;
    }

    /**
     * Get the state of the storage.
     * @return the IBlockState.
     */
    public IForgeBlockState getState()
    {
        return state;
    }

    /**
     * Get the entity of the storage.
     * @return the TileEntity.
     */
    @Nullable
    public TileEntity getEntity()
    {
        return entity;
    }
}
