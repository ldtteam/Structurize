package com.ldtteam.structurize.proxy;

import java.io.File;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * Basic proxy interface.
 */
public interface IProxy
{
    /**
     * Opens a build tool window.
     *
     * @param pos coordinates.
     * @param groundstyle one of the GROUNDSTYLE_ values.
     */
    default void openBuildToolWindow(final BlockPos pos, final int groundstyle)
    {
    }

    /**
     * Opens a shape tool window.
     *
     * @param pos coordinates.
     */
    default void openShapeToolWindow(final BlockPos pos)
    {
    }

    /**
     * Get the file representation of the additional schematics' folder.
     *
     * @return the folder for the schematic
     */
    @Nullable
    default File getSchematicsFolder()
    {
        return null;
    }

    @Nullable
    default BlockState getBlockStateFromWorld(final BlockPos pos)
    {
        return null;
    }
}
