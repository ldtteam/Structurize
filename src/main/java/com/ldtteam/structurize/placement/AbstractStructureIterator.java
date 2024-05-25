package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.TriPredicate;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * This is the blueprint iterator.
 * It's a helper class used to track the progress of one iteration over the structure.
 */
public abstract class AbstractStructureIterator extends AbstractBlueprintIterator
{

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
    public AbstractStructureIterator(final IStructureHandler structureHandler)
    {
        this(structureHandler, new BlockPos(structureHandler.getBluePrint().getSizeX(), structureHandler.getBluePrint().getSizeY(), structureHandler.getBluePrint().getSizeZ()));
    }

    /**
     * Initialize the blueprint iterator with the structure handler.
     * @param size the size.
     * @param structureHandler the structure handler.
     */
    public AbstractStructureIterator(final IStructureHandler structureHandler, final BlockPos size)
    {
        this.structureHandler = structureHandler;
        this.size = size;
    }

    @Override
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

    @Override
    public BlueprintPositionInfo getBluePrintPositionInfo(final BlockPos localPos)
    {
        return structureHandler.getBluePrint().getBluePrintPositionInfo(localPos, includeEntities);
    }

    @Override
    public void includeEntities()
    {
        this.includeEntities = true;
    }

    @Override
    public void setRemoving()
    {
        this.isRemoving = true;
    }

    @Override
    public boolean isRemoving()
    {
        return isRemoving;
    }

    @Override
    public void reset()
    {
        progressPos.set(NULL_POS);
        includeEntities = false;
        isRemoving = false;
    }

    @Override
    public BlockPos getSize()
    {
        return size;
    }

    @Override
    public BlockPos getProgressPos()
    {
        return progressPos.immutable();
    }

    @Override
    protected IStructureHandler getStructureHandler()
    {
        return structureHandler;
    }
}
