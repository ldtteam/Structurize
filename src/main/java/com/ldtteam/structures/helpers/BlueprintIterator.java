package com.ldtteam.structures.helpers;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * This is the blueprint iterator.
 * It's a helper class used to track the progress of one iteration over the structure.
 */
public class BlueprintIterator
{
    /**
     * The position we use as our uninitialized value.
     */
    private static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);

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
     * Initialize the blueprint iterator with the structure handler.
     * @param structureHandler the size of the blueprint.
     */
    public BlueprintIterator(final IStructureHandler structureHandler)
    {
        this.structureHandler = structureHandler;
        this.size = new BlockPos(structureHandler.getBluePrint().getSizeX(), structureHandler.getBluePrint().getSizeY(), structureHandler.getBluePrint().getSizeZ());
    }

    /**
     * Increment progressPos.
     *
     * @return false if the all the block have been incremented through.
     */
    public boolean increment()
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
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Decrement progressPos.
     *
     * @return false if progressPos can't be decremented any more.
     */
    public boolean decrement()
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
                    return false;
                }
            }
        }

        return true;
    }

    public boolean increment(final Predicate<BlueprintPositionInfo> blockInfoPredicate, final boolean includeEntities)
    {
        int count = 0;
        do
        {
            final BlockPos worldPos = structureHandler.getPosition().subtract(structureHandler.getBluePrint().getPrimaryBlockOffset()).add(progressPos);
            final BlueprintPositionInfo info = structureHandler.getBluePrint().getBluePrintPositionInfo(progressPos, includeEntities);
            if(!increment())
            {
                return false;
            }
            else if (BlockUtils.areBlockStatesEqual(structureHandler.getBluePrint().getBlockInfoAsMap().get(progressPos).getState(), structureHandler.getWorld().getBlockState(worldPos)))
            {
                structureHandler.triggerSuccess(progressPos);
            }
            else if (!blockInfoPredicate.test(info))
            {
                return true;
            }
        }
        while (count < Structurize.getConfig().getCommon().maxBlocksChecked.get());

        return true;
    }


    public boolean decrement(final Predicate<BlueprintPositionInfo> blockInfoPredicate, final boolean includeEntities)
    {
        int count = 0;
        do
        {
            final BlockPos worldPos = structureHandler.getPosition().subtract(structureHandler.getBluePrint().getPrimaryBlockOffset()).add(progressPos);
            final BlueprintPositionInfo info = structureHandler.getBluePrint().getBluePrintPositionInfo(progressPos, includeEntities);
            if(!decrement())
            {
                return false;
            }
            else if (BlockUtils.areBlockStatesEqual(structureHandler.getBluePrint().getBlockInfoAsMap().get(progressPos).getState(), structureHandler.getWorld().getBlockState(worldPos)))
            {
                structureHandler.triggerSuccess(progressPos);
            }
            else if (!blockInfoPredicate.test(info))
            {
                return true;
            }
        }
        while (count < Structurize.getConfig().getCommon().maxBlocksChecked.get());

        return true;
    }

    /**
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    public void setProgressPos(@NotNull final BlockPos localPosition)
    {
        this.progressPos.setPos(localPosition.getX() % size.getX(),
          localPosition.getY() % size.getY(),
          localPosition.getZ() % size.getZ());
    }

    /**
     * Reset the progressPos.
     */
    public void reset()
    {
        BlockPosUtil.set(this.progressPos, NULL_POS);
    }
}
