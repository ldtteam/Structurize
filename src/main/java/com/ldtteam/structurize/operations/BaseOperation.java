package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.ITickedWorldOperation;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for handling the root mechanics of any operation.
 */
public abstract class BaseOperation implements ITickedWorldOperation
{
    /**
     * The change storage holder.
     */
    protected final ChangeStorage storage;

    /**
     * Default constructor.
     */
    protected BaseOperation(final ChangeStorage storage)
    {
        this.storage = storage;
    }

    @Override
    @NotNull
    public final ChangeStorage getChangeStorage()
    {
        return storage;
    }
}
