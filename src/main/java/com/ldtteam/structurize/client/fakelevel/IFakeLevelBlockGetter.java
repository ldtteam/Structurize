package com.ldtteam.structurize.client.fakelevel;

import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface IFakeLevelBlockGetter extends BlockGetter
{
    // TODO: change to int on next major

    short getSizeX();

    short getSizeZ();

    default int getMinSizeX()
    {
        return 0;
    }

    default int getMinSizeZ()
    {
        return 0;
    }

    default boolean isPosInside(final BlockPos pos)
    {
        return getMinSizeX() <= pos.getX() && pos.getX() < getSizeX() &&
            getMinSizeZ() <= pos.getZ() && pos.getZ() < getSizeZ() &&
            !isOutsideBuildHeight(pos);
    }

    default boolean isPosOutside(final BlockPos pos)
    {
        return !isPosInside(pos);
    }

    @Override
    default FluidState getFluidState(final BlockPos pos)
    {
        return isPosInside(pos) ? getBlockState(pos).getFluidState() : Fluids.EMPTY.defaultFluidState();
    }

    default void describeSelfInCrashReport(final CrashReportCategory category)
    {}

    default BlockState getRawBlockState(final BlockPos pos)
    {
        return isPosInside(pos) ? getBlockState(pos) : null;
    }

    default Function<BlockPos, @Nullable BlockState> getRawBlockStateFunction()
    {
        return this::getRawBlockState;
    }

    default AABB getAABB()
    {
        final BlockPos root = new BlockPos(getMinSizeX(), getMinBuildHeight(), getMinSizeZ());
        return new AABB(root, root.offset(getSizeX(), getHeight(), getSizeZ()));
    }
}
