package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.InventoryUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
    boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState);

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
    default ActionProcessingResult handle(
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete, final BlockPos centerPos)
    {
        /*
         * Do nothing...
         */
        return ActionProcessingResult.PASS;
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
    default ActionProcessingResult handle(
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete, final BlockPos centerPos, final PlacementSettings settings)
    {
        return handle(world, pos, blockState, tileEntityData, complete, centerPos);
    }

    /**
     * Method used to handle the processing of a Placement of a block.
     *
     * @param blueprint      the blueprint.
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param complete       place it complete (with or without substitution blocks etc).
     * @param centerPos      the central position of it.
     * @param settings       the settings to use to rotate or mirror it.
     * @return ACCEPT, DENY or IGNORE.
     */
    default ActionProcessingResult handle(final Blueprint blueprint,
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete, final BlockPos centerPos, final PlacementSettings settings)
    {
        return handle(world, pos, blockState, tileEntityData, complete, centerPos,settings);
    }

    /**
     * Handles the removal of the existing block in the world
     *
     * @param handler the actor removing the block
     * @param world the world this block belongs to
     * @param pos the position of the block
     * @param tileEntityData any tile entity data in the blueprint
     */
    default void handleRemoval(
      final IStructureHandler handler,
      final Level world,
      final BlockPos pos,
      final CompoundTag tileEntityData)
    {
        handleRemoval(handler, world, pos);
    }

    /**
     * Handles the removal of the existing block in the world
     *
     * @param handler the actor removing the block
     * @param world the world this block belongs to
     * @param pos the position of the block
     */
    default void handleRemoval(
      final IStructureHandler handler,
      final Level world,
      final BlockPos pos)
    {
        if (!handler.isCreative())
        {
            final List<ItemStack> items = BlockUtils.getBlockDrops(world, pos, 0, handler.getHeldItem());
            for (final ItemStack item : items)
            {
                InventoryUtils.transferIntoNextBestSlot(item, handler.getInventory());
            }
        }
        world.removeBlock(pos, false);
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
      final Level world,
      final BlockPos pos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete);

    /**
     * {@link #getRequiredItems} but computes the difference between the desired and existing block at the
     * same coordinates.
     *
     * @param world              the world
     * @param pos                the pos
     * @param blueprintState     the block state in the blueprint
     * @param blueprintBlockData the block NBT in the blueprint
     * @param complete           place it complete (with or without substitution blocks etc).
     * @return the list of missing items
     */
    default List<ItemStack> getRequiredItemsVsWorld(
            final Level world,
            final BlockPos pos,
            final BlockState blueprintState,
            @Nullable final CompoundTag blueprintBlockData,
            final boolean complete)
    {
        final BlockState worldState = world.getBlockState(pos);
        if (blueprintState.getBlock() != worldState.getBlock())
        {
            return getRequiredItems(world, pos, blueprintState, blueprintBlockData, complete);
        }

        final BlockEntity te = world.getBlockEntity(pos);
        final CompoundTag worldBlockData = te == null ? null : te.saveWithFullMetadata();

        final List<ItemStack> worldItems = getRequiredItems(world, pos, worldState, worldBlockData, complete);
        final List<ItemStack> localItems = getRequiredItems(world, pos, blueprintState, blueprintBlockData, complete);

        return ItemStackUtils.computeDelta(localItems, worldItems);
    }

    /**
     * Possible result of an IPlacementHandler call.
     */
    enum ActionProcessingResult
    {
        PASS,
        DENY,
        SUCCESS
    }
}
