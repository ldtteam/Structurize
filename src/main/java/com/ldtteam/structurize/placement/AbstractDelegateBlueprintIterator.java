package com.ldtteam.structurize.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import net.minecraft.core.BlockPos;

/**
 * This is a base class for BlueprintIterators based on a delegated iterator
 * All methods are implemented through the delegate. Subclasses are expected to override one or multiple of the methods, to override logic
 */
public abstract class AbstractDelegateBlueprintIterator extends AbstractBlueprintIterator
{

    /**
     * The delegate blueprint iterator
     */
    protected AbstractBlueprintIterator delegate;

    /**
     * Initialise the blueprint iterator with a delegate blueprint iterator
     * @param delegate The delegate blueprint iterator
     */
    public AbstractDelegateBlueprintIterator(AbstractBlueprintIterator delegate)
    {
        this.delegate = delegate;
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
    public boolean isRemoving()
    {
        return delegate.isRemoving();
    }

    @Override
    public void reset()
    {
        delegate.reset();
    }

    @Override
    public BlockPos getSize()
    {
        return delegate.getSize();
    }

    @Override
    public BlockPos getProgressPos()
    {
        return delegate.getProgressPos();
    }

    @Override
    protected IStructureHandler getStructureHandler()
    {
        return delegate.getStructureHandler();
    }
}
