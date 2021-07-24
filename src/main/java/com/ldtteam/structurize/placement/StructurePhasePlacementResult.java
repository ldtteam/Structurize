package com.ldtteam.structurize.placement;

import net.minecraft.core.BlockPos;


/**
 * Possible placement for the invocation fo structure placement.
 */
public class StructurePhasePlacementResult
{
    /**
     * The result of the placement.
     */
    private final BlockPos iteratorPos;

    /**
     * The last BlockPlacementResult.
     */
    private final BlockPlacementResult result;

    /**
     * Create a placement result object.
     * @param iteratorPos the position the iterator is at.
     * @param result the last block placement result
     */
    public StructurePhasePlacementResult(final BlockPos iteratorPos, final BlockPlacementResult result)
    {
        this.iteratorPos = iteratorPos;
        this.result = result;
    }

    /**
     * Get the iterator pos.
     * @return the pos to restart from.
     */
    public BlockPos getIteratorPos()
    {
        return iteratorPos;
    }

    /**
     * Get the last block placement result.
     * @return the result.
     */
    public BlockPlacementResult getBlockResult()
    {
        return result;
    }
}
