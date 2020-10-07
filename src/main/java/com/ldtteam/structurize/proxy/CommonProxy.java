package com.ldtteam.structurize.proxy;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;

/**
 * CommonProxy of the structurize mod (Server and Client).
 */
public class CommonProxy implements IProxy
{
    @Override
    public boolean isClient()
    {
        return false;
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openScanToolWindow(final BlockPos pos1, final BlockPos pos2, final Optional<BlockPos> anchorPos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openShapeToolWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public File getSchematicsFolder()
    {
        return null;
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        return ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.getById(dimension));
    }

    @Override
    public void openMultiBlockWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void openPlaceholderBlockWindow(final BlockPos pos)
    {
        /*
         * Intentionally left empty.
         */
    }
}
