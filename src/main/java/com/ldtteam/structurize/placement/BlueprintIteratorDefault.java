package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import net.minecraft.util.math.BlockPos;

/**
 * This is the default blueprint iterator.
 * It's a helper class used to track the progress of one iteration over the structure.
 */
public class BlueprintIteratorDefault extends AbstractBlueprintIterator
{
    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param structureHandler the structure handler.
     */
    public BlueprintIteratorDefault(final IStructureHandler structureHandler)
    {
        super(structureHandler, new BlockPos(structureHandler.getBluePrint().getSizeX(), structureHandler.getBluePrint().getSizeY(), structureHandler.getBluePrint().getSizeZ()));
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
}
