package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import net.minecraft.util.math.BlockPos;

/**
 * Iterator which walks the structure from outside in , in a clockwise rotation
 */
public class BlueprintIteratorInwardCircleHeight extends AbstractBlueprintIterator
{
    private final BlockPos topRightCorner;

    public BlueprintIteratorInwardCircleHeight(final IStructureHandler structureHandler)
    {
        super(structureHandler);
        topRightCorner = size.add(-1, -1, -1);
    }

    @Override
    public Result increment()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(0, 0, 0);
        }

        final BlockPos next = BlockPosUtil.getNextPosInCircleFrom(progressPos, BlockPos.ZERO, topRightCorner, 4);
        if (next.equals(progressPos))
        {
            this.reset();
            return Result.AT_END;
        }

        BlockPosUtil.set(progressPos, next);
        return Result.NEW_BLOCK;
    }

    @Override
    public Result decrement()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(0, topRightCorner.getY(), 0);
        }

        progressPos.setY(topRightCorner.getY() - progressPos.getY());
        final BlockPos next = BlockPosUtil.getNextPosInCircleFrom(progressPos, BlockPos.ZERO, topRightCorner, 1);

        if (next == progressPos)
        {
            this.reset();
            return Result.AT_END;
        }

        progressPos.setPos(next.getX(), topRightCorner.getY() - next.getY(), next.getZ());
        return Result.NEW_BLOCK;
    }
}
