package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.api.constants.Constants.UPDATE_FLAG;
import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.getItemsFromTileEntity;
import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

/**
 * Handle the substitution handler.
 */
public class BlockTagSubstitutionPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() == ModBlocks.blockTagSubstitution.get();
    }

    @Override
    public ActionProcessingResult handle(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        if (placementContext.fancyPlacement())
        {
            if (tileEntityData != null && BlockEntity.loadStatic(pos, blockState, tileEntityData, world.registryAccess()) instanceof BlockEntityTagSubstitution tagEntity)
            {
                final IPlacementHandler placementHandler = PlacementHandlers.getHandler(world, pos, tagEntity.getReplacement().blockState());
                if (placementHandler != this)
                {
                    return placementHandler.handle(world, pos, tagEntity.getReplacement().blockState(), tagEntity.getReplacement().serializedBE().orElse(null), placementContext);
                }
                else
                {
                    Log.getLogger().warn("Blueprint contains faulty tag substitution block with recursive data.");
                    return ActionProcessingResult.PASS;
                }
            }
            else
            {
                Log.getLogger().warn("Blueprint contains faulty tag substitution block without data.");
                return ActionProcessingResult.PASS;
            }
        }
        else
        {
            // Normal placement with full handler.
            if (world.getBlockState(pos).equals(blockState))
            {
                world.removeBlock(pos, false);
                world.setBlock(pos, blockState, UPDATE_FLAG);
                if (tileEntityData != null)
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
                }
                return ActionProcessingResult.PASS;
            }

            if (!world.setBlock(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
            }
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public boolean doesWorldStateMatchBlueprintState(final BlockState worldState, final BlockState blueprintState, final Tuple<BlockEntity, CompoundTag> blockEntityData, final @NotNull IPlacementContext placementContext)
    {
        if (placementContext.fancyPlacement())
        {
            if (blockEntityData != null && BlockEntity.loadStatic(BlockPos.ZERO, blueprintState, blockEntityData.getB(), placementContext.getBluePrint().getRegistryAccess()) instanceof BlockEntityTagSubstitution tagEntity)
            {
                try
                {
                    final IPlacementHandler placementHandler = PlacementHandlers.getHandler(null, BlockPos.ZERO, tagEntity.getReplacement().blockState());
                    if (placementHandler != this)
                    {
                        final CompoundTag tileEntityData = tagEntity.getReplacement().serializedBE().orElse(null);
                        Tuple<BlockEntity, CompoundTag> updatedTETuple = null;
                        if (tileEntityData != null && !tileEntityData.isEmpty())
                        {
                            updatedTETuple = new Tuple<>(blockEntityData.getA(), tileEntityData);
                        }

                        return placementHandler.doesWorldStateMatchBlueprintState(worldState, tagEntity.getReplacement().blockState(), updatedTETuple, placementContext);
                    }
                    else
                    {
                        Log.getLogger().warn("Blueprint contains faulty tag substitution block with recursive data.");
                        return true;
                    }
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Blueprint contains tag substitution handler with block that needs world in match. This is deprecated. We will be moving to state only matching in the future.");
                    return true;
                }
            }
            else
            {
                Log.getLogger().warn("Blueprint contains faulty tag substitution block without data.");
                return true;
            }
        }

        // Always paste over to repair data if necessary.
        return false;
    }

    @Override
    public List<ItemStack> getRequiredItems(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        if (placementContext.fancyPlacement())
        {
            if (tileEntityData != null && BlockEntity.loadStatic(pos, blockState, tileEntityData, world.registryAccess()) instanceof BlockEntityTagSubstitution tagEntity)
            {
                final IPlacementHandler placementHandler = PlacementHandlers.getHandler(world, pos, tagEntity.getReplacement().blockState());
                if (placementHandler != this)
                {
                    return placementHandler.getRequiredItems(world, pos, tagEntity.getReplacement().blockState(), tagEntity.getReplacement().serializedBE().orElse(null), placementContext);
                }
                else
                {
                    Log.getLogger().warn("Blueprint contains faulty tag substitution block with recursive data.");
                    return Collections.emptyList();
                }
            }
            else
            {
                Log.getLogger().warn("Blueprint contains faulty tag substitution block without data.");
                return Collections.emptyList();
            }
        }

        final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState, world));
        itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
        itemList.removeIf(ItemStackUtils::isEmpty);
        return itemList;
    }
}
