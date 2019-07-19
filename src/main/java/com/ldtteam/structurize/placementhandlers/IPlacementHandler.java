package com.ldtteam.structurize.placementhandlers;

import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handler for all kinds of placements.
 */
public interface IPlacementHandler
{
    /**
     * Check if a placement handler can handle a certain block.
     *
     * @param world      the world.
     * @param pos        the position.
     * @param blockState the blockState.
     * @return true if so.
     */
    boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState);

    /**
     * Method used to handle the processing of a Placement of a block.
     *
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param complete       place it complete (with or without substitution blocks etc).
     * @param centerPos      centerPos the central position of it.
     * @return ACCEPT, DENY or IGNORE.
     */
    default Object handle(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final IBlockState blockState,
      @Nullable final CompoundNBT tileEntityData,
      final boolean complete, final BlockPos centerPos)
    {
        /*
         * Do nothing...
         */
        return Blocks.AIR;
    }

    /**
     * Method used to handle the processing of a Placement of a block.
     *
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param complete       place it complete (with or without substitution blocks etc).
     * @param centerPos      the central position of it.
     * @param settings       the settings to use to rotate or mirror it.
     * @return ACCEPT, DENY or IGNORE.
     */
    default Object handle(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final IBlockState blockState,
      @Nullable final CompoundNBT tileEntityData,
      final boolean complete, final BlockPos centerPos, final PlacementSettings settings)
    {
        return this.handle(world, pos, blockState, tileEntityData, complete, centerPos);
    }

    /**
     * Method used to get the required items to place a block.
     *
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param complete       place it complete (with or without substitution blocks etc).
     * @return the list of items.
     */
    List<ItemStack> getRequiredItems(
      @NotNull final World world,
      @NotNull final BlockPos pos,
      @NotNull final IBlockState blockState,
      @Nullable final CompoundNBT tileEntityData,
      final boolean complete);

    /**
     * Possible result of an IPlacementHandler call.
     */
    enum ActionProcessingResult
    {
        ACCEPT,
        DENY
    }
}
