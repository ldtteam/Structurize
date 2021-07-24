package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.InventoryUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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
    boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState);

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
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
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
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete, final BlockPos centerPos, final PlacementSettings settings)
    {
        return handle(world, pos, blockState, tileEntityData, complete, centerPos);
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
      @NotNull final IStructureHandler handler,
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final CompoundTag tileEntityData)
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
      @NotNull final IStructureHandler handler,
      @NotNull final Level world,
      @NotNull final BlockPos pos)
    {
        if (!handler.isCreative())
        {
            final List<ItemStack> items = BlockUtils.getBlockDrops(world, pos, 0, handler.getHeldItem());
            for (final ItemStack item : items)
            {
                InventoryUtils.transferIntoNextBestSlot(item, handler.getInventory());
            }
        }
        else if (world.getBlockEntity(pos) != null)
        {
            world.removeBlockEntity(pos);
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
      @NotNull final Level world,
      @NotNull final BlockPos pos,
      @NotNull final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      final boolean complete);

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
