package com.ldtteam.structurize.operations;

import com.ldtteam.structurize.util.ChangeStorage;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    ChangeStorage getChangeStorage();
}
