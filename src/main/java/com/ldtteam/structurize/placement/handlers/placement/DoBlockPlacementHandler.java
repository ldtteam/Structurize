package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.domumornamentum.block.IMateriallyTexturedBlock;
import com.ldtteam.domumornamentum.block.decorative.PillarBlock;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.entity.block.IMateriallyTexturedBlockEntity;
import com.ldtteam.domumornamentum.entity.block.MateriallyTexturedBlockEntity;
import com.ldtteam.domumornamentum.util.BlockUtils;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;
import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class DoBlockPlacementHandler implements IPlacementHandler
{
    @Override
    public boolean canHandle(@NotNull final Level world, @NotNull final BlockPos pos, @NotNull final BlockState blockState)
    {
        return blockState.getBlock() instanceof IMateriallyTexturedBlock;
    }

    @Override
    public ActionProcessingResult handle(
        @NotNull final Level world,
        @NotNull final BlockPos pos,
        @NotNull final BlockState blockState,
        @Nullable final CompoundTag tileEntityData,
        @NotNull final IPlacementContext placementContext)
    {
        BlockState placementState = blockState;
        if (blockState.getBlock() instanceof WallBlock
            || blockState.getBlock() instanceof FenceBlock
            || blockState.getBlock() instanceof IronBarsBlock
            || blockState.getBlock() instanceof PillarBlock)
        {
            try
            {
                final BlockState tempState = blockState.getBlock().getStateForPlacement(
                    new BlockPlaceContext(world, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY,
                        new BlockHitResult(new Vec3(0, 0, 0), Direction.DOWN, pos, true)));
                if (tempState != null)
                {
                    placementState = tempState;
                }
            }
            catch (final Exception ex)
            {
                // Noop
            }
        }

        if (world.getBlockState(pos).equals(placementState))
        {
            // Need to remove first to be able to place over.
            world.removeBlock(pos, false);
        }

        if (!world.setBlock(pos, placementState, UPDATE_FLAG))
        {
            return ActionProcessingResult.PASS;
        }

        if (tileEntityData != null)
        {
            try
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
                placementState.getBlock().setPlacedBy(world, pos, placementState, null, placementState.getBlock().getCloneItemStack(placementState,
                    new BlockHitResult(new Vec3(0, 0, 0), Direction.NORTH, pos, false), world, pos, null));
            }
            catch (final Exception ex)
            {
                Log.getLogger().warn("Unable to place TileEntity");
            }
        }

        return ActionProcessingResult.SUCCESS;
    }

    @Override
    public boolean doesWorldStateMatchBlueprintState(final BlockState worldState, final BlockState blueprintState, final Tuple<BlockEntity, CompoundTag> blockEntityData, final @NotNull IPlacementContext structureHandler)
    {
        if (blueprintState.getBlock() == worldState.getBlock()
            && (blueprintState.getBlock() instanceof WallBlock
                || blueprintState.getBlock() instanceof FenceBlock
                || blueprintState.getBlock() instanceof IronBarsBlock
                || blueprintState.getBlock() instanceof FenceGateBlock)
            && compareBEData(blockEntityData))
        {
            return true;
        }

        return worldState.equals(blueprintState) && compareBEData(blockEntityData);
    }

    /**
     * Check if the Block Entity data matches for DO blocks.
     * @param blockEntityData check on the block entity textures.
     * @return true if so.
     */
    public static boolean compareBEData(final Tuple<BlockEntity, CompoundTag> blockEntityData)
    {
        if (blockEntityData != null)
        {
            if (blockEntityData.getA() instanceof final IMateriallyTexturedBlockEntity mtbe && blockEntityData.getB().contains("textureData"))
            {
                return mtbe.getTextureData().equals(MaterialTextureData.deserializeFromNBT(blockEntityData.getB().getCompound("textureData")));
            }
        }
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
        final List<ItemStack> itemList = new ArrayList<>();
        if (tileEntityData != null)
        {
            BlockPos blockpos = new BlockPos(tileEntityData.getInt("x"), tileEntityData.getInt("y"), tileEntityData.getInt("z"));
            final BlockEntity tileEntity = BlockEntity.loadStatic(blockpos, blockState, tileEntityData);
            if (tileEntity == null)
            {
                return Collections.emptyList();
            }
            itemList.add(BlockUtils.getMaterializedItemStack(null, tileEntity));
        }
        itemList.removeIf(ItemStackUtils::isEmpty);
        return itemList;
    }

    @Override
    public void handleRemoval(
        final IStructureHandler handler,
        final Level world,
        final BlockPos pos)
    {
        if (!handler.isCreative())
        {
            final List<ItemStack> items = com.ldtteam.structurize.util.BlockUtils.getBlockDrops(world, pos, 0, handler.getHeldItem());
            for (final ItemStack item : items)
            {
                InventoryUtils.transferIntoNextBestSlot(item, handler.getInventory());
            }
        }
        world.removeBlock(pos, false);
    }
}

