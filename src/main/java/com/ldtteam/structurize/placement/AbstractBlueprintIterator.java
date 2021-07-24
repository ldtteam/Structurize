package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * This is the blueprint iterator.
 * It's a helper class used to track the progress of one iteration over the structure.
 */
public abstract class AbstractBlueprintIterator
{
    /**
     * The position we use as our uninitialized value.
     */
    public static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);

    /**
     * The Structure position we are at. Defaulted to NULL_POS.
     */
    protected final BlockPos.MutableBlockPos progressPos = new BlockPos.MutableBlockPos(-1, -1, -1);

    /**
     * The size of the structure.
     */
    protected final BlockPos size;

    /**
     * The structure handler.
     */
    private final IStructureHandler structureHandler;

    /**
     * If entity info is required.
     */
    private boolean includeEntities;

    /**
     * If structure removal step.
     */
    private boolean isRemoving;

    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param structureHandler the structure handler.
     */
    public AbstractBlueprintIterator(final IStructureHandler structureHandler)
    {
        this(structureHandler, new BlockPos(structureHandler.getBluePrint().getSizeX(), structureHandler.getBluePrint().getSizeY(), structureHandler.getBluePrint().getSizeZ()));
    }

    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param size the size.
     * @param structureHandler the structure handler.
     */
    public AbstractBlueprintIterator(final IStructureHandler structureHandler, final BlockPos size)
    {
        this.structureHandler = structureHandler;
        this.size = size;
    }

    /**
     * Increment the structure with a certain skipCondition (jump over blocks that fulfill skipCondition).
     * @param skipCondition the skipCondition.
     * @return Result of increment.
     */
    public Result increment(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition)
    {
        return iterateWithCondition(skipCondition, this::increment);
    }

    /**
     * Increment method, create in implementation.
     * @return increment the structure position (y++).
     */
    public abstract Result increment();

    /**
     * Decrement method, create in implementation.
     * @return decrement the structure position (y--).
     */
    public abstract Result decrement();

    /**
     * Decrement the structure with a certain skipCondition (jump over blocks that fulfill skipCondition).
     * @param skipCondition the skipCondition.
     * @return Result of decrement.
     */
    public Result decrement(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition)
    {
        return iterateWithCondition(skipCondition, this::decrement);
    }

    /**
     * Execute a supplier function to avoid duplicate code for increment and decrement functions.
     * @param skipCondition the skipCondition.
     * @param function the supplier function.
     * @return the Result.
     */
    private Result iterateWithCondition(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition, final Supplier<Result> function)
    {
        int count = 0;
        do
        {
            if(function.get() == Result.AT_END)
            {
                return Result.AT_END;
            }
            final BlockPos worldPos = structureHandler.getProgressPosInWorld(progressPos);
            final BlueprintPositionInfo info = getBluePrintPositionInfo(progressPos);

            if (skipCondition.test(info, worldPos, structureHandler))
            {
                continue;
            }
            else if (!isRemoving && BlockUtils.areBlockStatesEqual(info.getBlockInfo().getState(), structureHandler.getWorld().getBlockState(worldPos), structureHandler::replaceWithSolidBlock, structureHandler.fancyPlacement(), structureHandler::shouldBlocksBeConsideredEqual) && info.getEntities().length == 0)
            {
                structureHandler.triggerSuccess(progressPos, Collections.emptyList(), false);
                continue;
            }
            return Result.NEW_BLOCK;
        }
        while (count++ < structureHandler.getMaxBlocksCheckedPerCall());

        return Result.CONFIG_LIMIT;
    }

    /**
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    public void setProgressPos(final BlockPos localPosition)
    {
        if (localPosition.equals(NULL_POS))
        {
            this.progressPos.set(localPosition);
        }
        else
        {
            this.progressPos.set(localPosition.getX() % size.getX(),
              localPosition.getY() % size.getY(),
              localPosition.getZ() % size.getZ());
        }
    }

    /**
     * Get the blueprint info from the position.
     * @param localPos the position.
     * @return the info object.
     */
        public BlueprintPositionInfo getBluePrintPositionInfo(final BlockPos localPos)
    {
        return structureHandler.getBluePrint().getBluePrintPositionInfo(localPos, includeEntities);
    }

    /**
     * Set the iterator to include entities.
     */
    public void includeEntities()
    {
        this.includeEntities = true;
    }

    /**
     * Set the iterator to removal mode.
     */
    public void setRemoving()
    {
        this.isRemoving = true;
    }

    /**
     * Reset the progressPos.
     */
    public void reset()
    {
        BlockPosUtil.set(this.progressPos, NULL_POS);
        includeEntities = false;
        isRemoving = false;
    }

    /**
     * Get the progress pos of the iterator.
     * @return the progress pos.
     */
    public BlockPos getProgressPos()
    {
        return new BlockPos(progressPos);
    }

    /**
     * The different results when advancing the structure.
     */
    public enum Result
    {
        NEW_BLOCK,
        AT_END,
        CONFIG_LIMIT
    }
}
