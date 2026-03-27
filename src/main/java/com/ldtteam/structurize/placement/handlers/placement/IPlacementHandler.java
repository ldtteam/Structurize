package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.InventoryUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handler for all kinds of placements.
 */
public interface IPlacementHandler
{
    static boolean doesWorldStateMatchBlueprintState(@NotNull BlockInfo blockInfo, @NotNull BlockPos worldPos, @NotNull IStructureHandler structureHandler)
    {
        final IPlacementHandler placementHandler = PlacementHandlers.getHandler(structureHandler.getWorld(), worldPos, blockInfo.getState());
        Tuple<BlockEntity, CompoundTag> blockEntityData = null;
        if (blockInfo.hasTileEntityData())
        {
            final BlockEntity blockEntity = structureHandler.getWorld().getBlockEntity(worldPos);
            if (blockEntity == null)
            {
                return false;
            }
            blockEntityData = new Tuple<>(blockEntity, blockInfo.getTileEntityData());
        }

        return placementHandler.doesWorldStateMatchBlueprintState(structureHandler.getWorld().getBlockState(worldPos), blockInfo.getState(), blockEntityData, structureHandler);
    }

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
     * @param worldPos            the position.
     * @param blockState     the blockState.
     * @param tileEntityData the placer of the block.
     * @param placementContext extended placement context.
     * @return ACCEPT, DENY or IGNORE.
     */
    ActionProcessingResult handle(
      final Level world,
      final BlockPos worldPos,
      final BlockState blockState,
      @Nullable final CompoundTag tileEntityData,
      @NotNull final IPlacementContext placementContext);

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
     * @param placementContext the placement context.
     * @return the list of items.
     */
    List<ItemStack> getRequiredItems(
        final Level world,
        final BlockPos pos,
        final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext);

    /**
     * Method used to compare blueprint state with world state to detect if changes are necessary.
     * @return true if world state matches blueprint state.
     */
    boolean doesWorldStateMatchBlueprintState(final BlockState worldState, final BlockState blueprintState, @Nullable final Tuple<BlockEntity, CompoundTag> blockEntityData,  @NotNull final IPlacementContext placementContext);

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
