package com.ldtteam.structurize.storage.rendering.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
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

    @NotNull
    public BlockPos getPos1()
    {
        return pos1;
    }

    @NotNull
    public BlockPos getPos2()
    {
        return pos2;
    }

    @NotNull
    public BoundingBox getBounds()
    {
        return BoundingBox.fromCorners(pos1, pos2);
    }

    @NotNull
    public AABB getAABB()
    {
        return AABB.of(getBounds());
    }

    @NotNull
    public Optional<BlockPos> getAnchor()
    {
        return anchor;
    }

    public void setAnchor(final @NotNull Optional<BlockPos> anchor)
    {
        this.anchor = anchor;
    }
}
