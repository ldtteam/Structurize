package com.ldtteam.structures.helpers;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * This is the blueprint iterator.
 * It's a helper class used to track the progress of one iteration over the structure.
 */
public class BlueprintIterator
{
    /**
     * The position we use as our uninitialized value.
     */
    public static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);

    /**
     * The Structure position we are at. Defaulted to NULL_POS.
     */
    private final BlockPos.Mutable progressPos = new BlockPos.Mutable(-1, -1, -1);

    /**
     * The size of the structure.
     */
    private final BlockPos size;

    /**
     * The structure handler.
     */
    private final IStructureHandler structureHandler;

    /**
     * If entity info is required.
     */
    private boolean includeEntities;

    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param structureHandler the structure handler.
     */
    public BlueprintIterator(final IStructureHandler structureHandler)
    {
        this(structureHandler, new BlockPos(structureHandler.getBluePrint().getSizeX(), structureHandler.getBluePrint().getSizeY(), structureHandler.getBluePrint().getSizeZ()));
    }

    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param size the size.
     * @param structureHandler the structure handler.
     */
    public BlueprintIterator(final IStructureHandler structureHandler, final BlockPos size)
    {
        this.structureHandler = structureHandler;
        this.size = size;
    }

    /**
     * Increment progressPos.
     *
     * @return false if the all the block have been incremented through.
     */
    public Result increment()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(-1, 0, 0);
        }

        this.progressPos.setPos(this.progressPos.getX() + 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() >= this.size.getX())
        {
            this.progressPos.setPos(0, this.progressPos.getY(), this.progressPos.getZ() + 1);
            if (this.progressPos.getZ() >= this.size.getZ())
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() + 1, 0);
                if (this.progressPos.getY() >= this.size.getY())
                {
                    this.reset();
                    return Result.AT_END;
                }
            }
        }

        return Result.NEW_BLOCK;
    }

    /**
     * Decrement progressPos.
     *
     * @return false if progressPos can't be decremented any more.
     */
    public Result decrement()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(this.size.getX(), this.size.getY() - 1, this.size.getZ() - 1);
        }

        this.progressPos.setPos(this.progressPos.getX() - 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() <= -1)
        {
            this.progressPos.setPos(this.size.getX() - 1, this.progressPos.getY(), this.progressPos.getZ() - 1);
            if (this.progressPos.getZ() <= -1)
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() - 1, this.size.getZ() - 1);
                if (this.progressPos.getY() <= -1)
                {
                    this.reset();
                    return Result.AT_END;
                }
            }
        }

        return Result.NEW_BLOCK;
    }

    /**
     * Increment the structure with a certain skipCondition (jump over blocks that fulfill skipCondition).
     * @param skipCondition the skipCondition.
     * @return Result of increment.
     */
    public Result increment(final TriPredicate<BlueprintPositionInfo, BlockPos, World> skipCondition)
    {
        return iterateWithCondition(skipCondition, this::increment);
    }

    /**
     * Decrement the structure with a certain skipCondition (jump over blocks that fulfill skipCondition).
     * @param skipCondition the skipCondition.
     * @return Result of decrement.
     */
    public Result decrement(final TriPredicate<BlueprintPositionInfo, BlockPos, World> skipCondition)
    {
        return iterateWithCondition(skipCondition, this::decrement);
    }

    /**
     * Execute a supplier function to avoid duplicate code for increment and decrement functions.
     * @param skipCondition the skipCondition.
     * @param function the supplier function.
     * @return the Result.
     */
    private Result iterateWithCondition(final TriPredicate<BlueprintPositionInfo, BlockPos, World> skipCondition, final Supplier<Result> function)
    {
        int count = 0;
        do
        {
            if(function.get() == Result.AT_END)
            {
                return Result.AT_END;
            }
            final BlockPos worldPos = structureHandler.getWorldPos().subtract(structureHandler.getBluePrint().getPrimaryBlockOffset()).add(progressPos);
            final BlueprintPositionInfo info = getBluePrintPositionInfo();

            if (BlockUtils.areBlockStatesEqual(info.getBlockInfo().getState(), structureHandler.getWorld().getBlockState(worldPos), structureHandler::replaceWithSolidBlock, structureHandler.fancyPlacement()) && info.getEntities().length == 0)
            {
                structureHandler.triggerSuccess(progressPos);
                continue;
            }
            else if (skipCondition.test(info, worldPos, structureHandler.getWorld()))
            {
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
    public void setProgressPos(@NotNull final BlockPos localPosition)
    {
        if (localPosition.equals(NULL_POS))
        {
            this.progressPos.setPos(localPosition);
        }
        else
        {
            this.progressPos.setPos(localPosition.getX() % size.getX(),
              localPosition.getY() % size.getY(),
              localPosition.getZ() % size.getZ());
        }
    }

    /**
     * Get the blueprint info from the position.
     * @return the info object.
     */
    @NotNull
    public BlueprintPositionInfo getBluePrintPositionInfo()
    {
        return structureHandler.getBluePrint().getBluePrintPositionInfo(progressPos, includeEntities);
    }

    /**
     * Set the iterator to include entities.
     */
    public void includeEntities()
    {
        this.includeEntities = true;
    }

    /**
     * Reset the progressPos.
     */
    public void reset()
    {
        BlockPosUtil.set(this.progressPos, NULL_POS);
        includeEntities = false;
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
