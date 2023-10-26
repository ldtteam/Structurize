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

/**
 * Data source for {@link FakeLevel}
 */
public interface IFakeLevelBlockGetter extends BlockGetter
{
    // TODO: change getSizeX/Z to int on next major
    /**
     * @return width for X axis
     */
    short getSizeX();

    /**
     * @return width for Z axis
     */
    short getSizeZ();

    /**
     * @return min X coord inclusive
     * @see    #getMinBuildHeight() equivalent
     */
    default int getMinX()
    {
        return 0;
    }

    @Override
    default int getMinBuildHeight()
    {
        return 0;
    }

    /**
     * @return min Z coord inclusive
     * @see    #getMinBuildHeight() equivalent
     */
    default int getMinZ()
    {
        return 0;
    }

    /**
     * @return max X coord exclusive
     * @see    #getMaxBuildHeight() equivalent
     */
    default int getMaxX()
    {
        return getMinX() + getSizeX();
    }

    /**
     * @return max Z coord exclusive
     * @see    #getMaxBuildHeight() equivalent
     */
    default int getMaxZ()
    {
        return getMinZ() + getSizeZ();
    }

    /**
     * @param  pos tested pos
     * @return     true if inside aabb
     * @see        #isOutsideBuildHeight(BlockPos) extension of
     */
    default boolean isPosInside(final BlockPos pos)
    {
        return getMinX() <= pos.getX() && pos.getX() < getMaxX() &&
            getMinBuildHeight() <= pos.getY() && pos.getY() < getMaxBuildHeight() &&
            getMinZ() <= pos.getZ() && pos.getZ() < getMaxZ();
    }

    /**
     * @param  pos tested pos
     * @return     true if outside aabb
     * @see        #isOutsideBuildHeight(BlockPos) extension of
     */
    default boolean isPosOutside(final BlockPos pos)
    {
        return !isPosInside(pos);
    }

    @Override
    default FluidState getFluidState(final BlockPos pos)
    {
        return isPosInside(pos) ? getBlockState(pos).getFluidState() : Fluids.EMPTY.defaultFluidState();
    }

    /**
     * To show who is this fake level in level crashes
     */
    default void describeSelfInCrashReport(final CrashReportCategory category)
    {}

    /**
     * @return null if pos is outside of aabb
     */
    default BlockState getRawBlockState(final BlockPos pos)
    {
        return isPosInside(pos) ? getBlockState(pos) : null;
    }

    /**
     * @return function useful temporary insert into existing world
     * @see #getRawBlockState(BlockPos)
     */
    default Function<BlockPos, @Nullable BlockState> getRawBlockStateFunction()
    {
        return this::getRawBlockState;
    }

    /**
     * @return aabb with end being blockpos-wise exclusive
     */
    default AABB getAABB()
    {
        return new AABB(getMinX(), getMinBuildHeight(), getMinZ(), getMaxX(), getMaxBuildHeight(), getMaxZ());
    }
}
