package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Simple placement context for non blueprint handling.
 */
public class SimplePlacementContext implements IPlacementContext
{
    /**
     * If placement should be fancy or complete.
     */
    private final boolean fancyPlacement;

    /**
     * Rotation mirror.
     */
    private final PlacementSettings rotationMirror;

    public SimplePlacementContext(final boolean fancyPlacement, final PlacementSettings rotationMirror)
    {
        this.fancyPlacement = fancyPlacement;
        this.rotationMirror = rotationMirror;
    }

    @Override
    public PlacementSettings getRotationMirror()
    {
        return rotationMirror;
    }

    @Override
    public boolean fancyPlacement()
    {
        return fancyPlacement;
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos, final Function<BlockPos, @Nullable BlockState> virtualBlocks)
    {
        return Blocks.DIRT.defaultBlockState();
    }

    @Override
    public BlockPos getCenterPos()
    {
        return BlockPos.ZERO;
    }

    @Override
    public Blueprint getBluePrint()
    {
        return null;
    }
}
