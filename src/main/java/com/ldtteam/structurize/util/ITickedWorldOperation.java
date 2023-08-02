package com.ldtteam.structurize.util;

import net.minecraft.server.level.ServerLevel;

public interface ITickedWorldOperation
{
    /**
     * Apply the operation on the world.
     *
     * @param world the world to apply them on.
     * @return true if finished.
     */
    boolean apply(ServerLevel world);

    /**
     * Get the current change storage of this operation.
     *
     * @return the ChangeStorage object.
     */
    ChangeStorage getChangeStorage();

    /**
     * Check if operation is an undo already.
     *
     * @return true if so.
     */
    boolean isUndoRedo();
}
