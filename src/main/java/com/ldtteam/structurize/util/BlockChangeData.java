package com.ldtteam.structurize.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Class which preserves a state change of a block and TE
 */
public class BlockChangeData
{
    /**
     * The state at a position.
     */
    private BlockState pre;

    /**
     * The state at a position.
     */
    private BlockState post;

    /**
     * The tileEntity at a position.
     */
    @Nullable
    private BlockEntity preTE = null;

    /**
     * The tileEntity at a position.
     */
    @Nullable
    private BlockEntity postTE = null;

    /**
     * Constructor for the storage.
     */
    public BlockChangeData()
    {

    }

    /**
     * Get the state of the storage.
     *
     * @return the BlockState.
     */
    public BlockState getPreState()
    {
        return pre;
    }

    /**
     * Get the state of the storage.
     *
     * @return the BlockState.
     */
    public BlockState getPostState()
    {
        return post;
    }

    /**
     * Get the entity of the storage.
     *
     * @return the TileEntity.
     */
    @Nullable
    public BlockEntity getPreTE()
    {
        return preTE;
    }

    /**
     * Get the entity of the storage.
     *
     * @return the TileEntity.
     */
    @Nullable
    public BlockEntity getPostTE()
    {
        return postTE;
    }

    public BlockChangeData withPreTE(final BlockEntity te)
    {
        this.preTE = te;
        return this;
    }

    public BlockChangeData withPostTE(final BlockEntity te)
    {
        postTE = te;
        return this;
    }

    public BlockChangeData withPreState(final BlockState state)
    {
        pre = state;
        return this;
    }

    public BlockChangeData withPostState(final BlockState state)
    {
        post = state;
        return this;
    }
}
