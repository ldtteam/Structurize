package com.ldtteam.structurize.storage.rendering.types;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Preview data for box contexts.
 */
public class BoxPreviewData
{
    @NotNull
    private final BlockPos pos1;

    @NotNull
    private final BlockPos pos2;

    @NotNull
    private Optional<BlockPos> anchor;

    private long expireTime = Long.MAX_VALUE;

    /**
     * Create a new box.
     * @param pos1 the first pos.
     * @param pos2 the second pos.
     * @param anchor the anchor of the box.
     */
    public BoxPreviewData(final @NotNull BlockPos pos1, final @NotNull BlockPos pos2, final @NotNull Optional<BlockPos> anchor)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.anchor = anchor;
    }

    public BlockPos getPos1()
    {
        return pos1;
    }

    public BlockPos getPos2()
    {
        return pos2;
    }

    public Optional<BlockPos> getAnchor()
    {
        return anchor;
    }

    public void setAnchor(final Optional<BlockPos> anchor)
    {
        this.anchor = anchor;
    }

    public boolean isExpired()
    {
        return System.currentTimeMillis() - expireTime > 0;
    }

    public void setExpireTime(final int seconds)
    {
        expireTime = System.currentTimeMillis() + seconds * 1000;
    }
}
