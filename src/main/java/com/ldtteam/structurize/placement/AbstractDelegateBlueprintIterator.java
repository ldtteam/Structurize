package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.TriPredicate;

/**
 * This is a base class for BlueprintIterators based on a delegated iterator
 * All methods are implemented through the delegate. Subclasses are expected to override one or multiple of the methods, to override logic
 */
public abstract class AbstractDelegateBlueprintIterator implements IBlueprintIterator
{

    /**
     * The delegate blueprint iterator
     */
    protected IBlueprintIterator delegate;

    /**
     * Initialise the blueprint iterator with a delegate blueprint iterator
     * @param delegate The delegate blueprint iterator
     */
    public AbstractDelegateBlueprintIterator(IBlueprintIterator delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public Result increment(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition)
    {
        return delegate.increment(skipCondition);
    }

    @Override
    public Result increment()
    {
        return delegate.increment();
    }

    @Override
    public Result decrement()
    {
        return delegate.decrement();
    }

    @Override
    public Result decrement(final TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> skipCondition)
    {
        return delegate.decrement(skipCondition);
    }

    @Override
    public void setProgressPos(final BlockPos localPosition)
    {
        delegate.setProgressPos(localPosition);
    }

    @Override
    public BlueprintPositionInfo getBluePrintPositionInfo(final BlockPos localPos)
    {
        return delegate.getBluePrintPositionInfo(localPos);
    }

    @Override
    public void includeEntities()
    {
        delegate.includeEntities();
    }

    @Override
    public void setRemoving()
    {
        delegate.setRemoving();
    }

    @Override
    public void reset()
    {
        delegate.reset();
    }

    @Override
    public BlockPos getProgressPos()
    {
        return delegate.getProgressPos();
    }
}
