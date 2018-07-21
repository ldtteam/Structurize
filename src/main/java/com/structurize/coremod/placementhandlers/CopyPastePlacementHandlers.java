package com.structurize.coremod.placementhandlers;

import com.structurize.api.compatibility.candb.ChiselAndBitsCheck;
import com.structurize.api.util.BlockUtils;
import com.structurize.api.util.ItemStackUtils;
import com.structurize.coremod.blocks.schematic.BlockSolidSubstitution;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.structurize.api.util.constant.Constants.UPDATE_FLAG;
import static com.structurize.coremod.placementhandlers.PlacementHandlers.getItemsFromTileEntity;
import static com.structurize.coremod.placementhandlers.PlacementHandlers.handleTileEntityPlacement;

/**
 * Contains all Structurize specific placement handlers.
 */
public final class CopyPastePlacementHandlers
{
    /**
     * Private constructor to hide implicit one.
     */
    private CopyPastePlacementHandlers()
    {
        /*
         * Intentionally left empty.
         */
    }

    public static void initHandlers()
    {
        PlacementHandlers.handlers.clear();
        PlacementHandlers.handlers.add(new PlacementHandlers.AirPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FirePlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.GrassPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.DoorPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BedPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.DoublePlantPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.SpecialBlockPlacementAttemptHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.FlowerPotPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.BlockGrassPathPlacementHandler());
        PlacementHandlers.handlers.add(new PlacementHandlers.StairBlockPlacementHandler());
        PlacementHandlers.handlers.add(new BlockSolidSubstitutionPlacementHandler());
        PlacementHandlers.handlers.add(new GeneralBlockPlacementHandler());
    }


    public static class BlockSolidSubstitutionPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return blockState.getBlock() instanceof BlockSolidSubstitution;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            final IBlockState newBlockState = BlockUtils.getSubstitutionBlockAtWorld(world, pos);
            if (complete)
            {
                if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }
            else
            {
                if (!world.setBlockState(pos, newBlockState, UPDATE_FLAG))
                {
                    return ActionProcessingResult.DENY;
                }
            }

            return newBlockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final IBlockState newBlockState = BlockUtils.getSubstitutionBlockAtWorld(world, pos);
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(newBlockState));
            return itemList;
        }
    }

    public static class GeneralBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState)
        {
            return true;
        }

        @Override
        public Object handle(
          @NotNull final World world,
          @NotNull final BlockPos pos,
          @NotNull final IBlockState blockState,
          @Nullable final NBTTagCompound tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.ACCEPT;
            }

            if (!world.setBlockState(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null)
            {
               handleTileEntityPlacement(tileEntityData, world, pos);
            }

            return blockState;
        }

        @Override
        public List<ItemStack> getRequiredItems(@NotNull final World world, @NotNull final BlockPos pos, @NotNull final IBlockState blockState, @Nullable final NBTTagCompound tileEntityData, final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            if (!ChiselAndBitsCheck.isChiselAndBitsBlock(blockState))
            {
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            }
            itemList.addAll(getItemsFromTileEntity(tileEntityData, world));
            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }
}
