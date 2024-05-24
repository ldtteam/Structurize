package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.TriPredicate;

public interface IBlueprintIterator
{
    /**
     * The position we use as our uninitialized value.
     */
    BlockPos NULL_POS = new BlockPos(-1, -1, -1);

    /**
     * Increment the structure with a certain skipCondition (jump over blocks that fulfill skipCondition).
     * @param skipCondition the skipCondition.
     * @return Result of increment.
     */
    Result increment(TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition);

    /**
     * Increment method, create in implementation.
     *
     * @return increment the structure position (y++).
     */
    Result increment();

    /**
     * Decrement method, create in implementation.
     *
     * @return decrement the structure position (y--).
     */
    Result decrement();

    /**
     * Decrement the structure with a certain skipCondition (jump over blocks that fulfill skipCondition).
     * @param skipCondition the skipCondition.
     * @return Result of decrement.
     */
    Result decrement(TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition);

    /**
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    void setProgressPos(BlockPos localPosition);

    /**
     * Get the blueprint info from the position.
     * @param localPos the position.
     * @return the info object.
     */
    BlueprintPositionInfo getBluePrintPositionInfo(BlockPos localPos);

    /**
     * Set the iterator to include entities.
     */
    void includeEntities();

    /**
     * Set the iterator to removal mode.
     */
    void setRemoving();

    /**
     * Reset the progressPos.
     */
    void reset();

    /**
     * Get the progress pos of the iterator.
     * @return the progress pos.
     */
    BlockPos getProgressPos();

    /**
     * The different results when advancing the structure.
     */
    enum Result
    {
        NEW_BLOCK,
        AT_END,
        CONFIG_LIMIT
    }
}
