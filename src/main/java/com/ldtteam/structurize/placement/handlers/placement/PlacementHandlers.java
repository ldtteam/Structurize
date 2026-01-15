package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.structurize.api.IRotatableBlockEntity;
import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.tag.ModTags;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.api.constants.Constants.UPDATE_FLAG;

/**
 * Class containing all placement handler implementations.
 * <p>
 * We suppress warning squid:S2972 which handles the max size of internal classes. This doesn't apply here since it wouldn't make sense extracting all of those in separate
 * classes.
 */
@SuppressWarnings("squid:S2972")
public final class PlacementHandlers
{
    public static final List<IPlacementHandler> handlers = new ArrayList<>();
    static
    {
        handlers.add(new AirPlacementHandler());
        handlers.add(new BlackListedBlockPlacementHandler());
        handlers.add(new FluidSubstitutionPlacementHandler());
        handlers.add(new FirePlacementHandler());
        handlers.add(new BlockGrassPathPlacementHandler());
        handlers.add(new GrassPlacementHandler());
        handlers.add(new DoorPlacementHandler());
        handlers.add(new BedPlacementHandler());
        handlers.add(new DoublePlantPlacementHandler());
        handlers.add(new SpecialBlockPlacementAttemptHandler());
        handlers.add(new FlowerPotPlacementHandler());
        handlers.add(new StairBlockPlacementHandler());
        handlers.add(new HopperClientLagPlacementHandler());
        handlers.add(new DripStoneBlockPlacementHandler());
        handlers.add(new FallingBlockPlacementHandler());
        handlers.add(new BannerPlacementHandler());
        handlers.add(new ContainerPlacementHandler());
        handlers.add(new GeneralBlockPlacementHandler());
    }

    /**
     * Allows for adding new handlers without having to clear the list
     * in other mods just to override one
     * @param handler the new handler to add
     * @param override the class to override if it can be found
     */
    public static void add(IPlacementHandler handler, Class<?> override)
    {
        for (int i = 0; i < handlers.size(); i++)
        {
            if (override.isInstance(handlers.get(i)))
            {
                handlers.set(i, handler);
                return;
            }
        }
        add(handler);
    }

    /**
     * Adds a handler to the start of the handlers list,
     * effectively overriding existing ones with similar
     * 'canHandle' functions because this one will evaluate before them
     * @param handler
     */
    public static void add(IPlacementHandler handler)
    {
        handlers.add(1, handler);
    }

    /**
     * Finds the appropriate {@link IPlacementHandler} for the given location.
     * @param world     the world.
     * @param worldPos  the world position.
     * @param newState  the blockstate being placed or removed.
     * @return          the appropriate handler.
     */
    public static IPlacementHandler getHandler(final Level world,
                                               final BlockPos worldPos,
                                               final BlockState newState)
    {
        for (final IPlacementHandler placementHandler : handlers)
        {
            if (placementHandler.canHandle(world, worldPos, newState))
            {
                return placementHandler;
            }
        }

        Log.getLogger().error("Unable to find any PlacementHandler for {}; this should be impossible.", newState.toString());
        return new GeneralBlockPlacementHandler();
    }
    /**
     * Private constructor to hide implicit one.
     */
    private PlacementHandlers()
    {
        /*
         * Intentionally left empty.
         */
    }

    public static class FluidSubstitutionPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(Level world, BlockPos pos, BlockState blockState)
        {
            return blockState.is(ModBlocks.blockFluidSubstitution);
        }

        @Override
        public List<ItemStack> getRequiredItems(
          Level world,
          BlockPos pos,
          BlockState blockState,
          @Nullable CompoundTag tileEntityData,
          boolean complete)
        {
            List<ItemStack> items = new ArrayList<>();

            if (complete)
            {
                // for scan tool, show the actual placeholder block
                items.add(new ItemStack(blockState.getBlock()));
            }
            else
            {
                // for build tool, water is free but lava needs a bucket
                if (BlockUtils.getFluidForDimension(world).getBlock() == Blocks.LAVA)
                {
                    items.add(new ItemStack(Items.LAVA_BUCKET));
                }
            }

            return items;
        }

        @Override
        public void handleRemoval(
          IStructureHandler handler,
          Level world,
          BlockPos pos,
          CompoundTag tileEntityData)
        {
            BlockState state = world.getBlockState(pos);
            // If there's no water there and there can be
            if (!(state.hasProperty(BlockStateProperties.WATERLOGGED)
             && !state.getValue(BlockStateProperties.WATERLOGGED)
             && BlockUtils.getFluidForDimension(world).getBlock() == Blocks.WATER))
            {
                handleRemoval(handler, world, pos);
            }
        }

        @Override
        public ActionProcessingResult handle(
            Level world,
            BlockPos pos,
            BlockState blockState,
            @Nullable CompoundTag tileEntityData,
            boolean complete,
            BlockPos centerPos,
            RotationMirror settings)
        {
            if (complete)
            {
                handleBlockPlacement(world, pos, ModBlocks.blockFluidSubstitution.get().defaultBlockState());
                return ActionProcessingResult.PASS;
            }

            if (world.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED))
            {
                handleBlockPlacement(world, pos, world.getBlockState(pos).setValue(BlockStateProperties.WATERLOGGED, true));
            }
            else
            {
                handleBlockPlacement(world, pos, BlockUtils.getFluidForDimension(world));
            }

            return ActionProcessingResult.PASS;
        }
    }

    public static class FirePlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.is(Blocks.FIRE);
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(new ItemStack(Items.FLINT_AND_STEEL, 1));
            return itemList;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            return simplePlacement(world, pos, blockState, null, null);
        }
    }

    public static class FallingBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof FallingBlock || blockState.getBlock() instanceof Fallable;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState, world));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);

            if (!BlockUtils.isAnySolid(world.getBlockState(pos.below())))
            {
                BlockPos posBelow = pos;
                BlockState supportBlockState = Blocks.DIRT instanceof Fallable ? Blocks.STONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                for (int i = 0; i < 10; i++) // try up to ten blocks below for solid worldgen
                {
                    posBelow = posBelow.below();
                    final boolean isFirstTest = i == 0;
                    final BlockState possibleSupport = BlockUtils.getWorldgenBlock(world, posBelow, bp -> isFirstTest ? blockState : null);
                    if (possibleSupport != null && BlockUtils.canBlockFloatInAir(possibleSupport) && !canHandle(world,
                        posBelow,
                        possibleSupport))
                    {
                        supportBlockState = possibleSupport;
                        break;
                    }
                }

                if (canHandle(world, pos, supportBlockState))
                {
                    Log.getLogger().warn("Unable to use: " + supportBlockState + " as support for a falling block, it is either a falling black itself or made fallable");
                }
                else
                {
                    itemList.addAll(getRequiredItemsForState(world, pos, supportBlockState, tileEntityData, complete));
                }
            }
            return itemList;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.PASS;
            }

            if (!BlockUtils.isAnySolid(world.getBlockState(pos.below())))
            {
                BlockPos posBelow = pos;
                BlockState supportBlockState = Blocks.DIRT.defaultBlockState();
                for (int i = 0; i < 10; i++) // try up to ten blocks below for solid worldgen
                {
                    posBelow = posBelow.below();
                    final boolean isFirstTest = i == 0;
                    final BlockState possibleSupport = BlockUtils.getWorldgenBlock(world, posBelow, bp -> isFirstTest ? blockState : null);
                    if (possibleSupport != null && BlockUtils.canBlockFloatInAir(possibleSupport))
                    {
                        supportBlockState = possibleSupport;
                        break;
                    }
                }
                handleBlockPlacement(world, pos.below(), supportBlockState);
            }

            if (!handleBlockPlacement(world, pos, blockState, settings, tileEntityData))
            {
                return ActionProcessingResult.DENY;
            }

            return ActionProcessingResult.SUCCESS;
        }
    }

    public static class GrassPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() == Blocks.GRASS_BLOCK || (blockState.getBlock() != Blocks.DIRT && blockState.is(BlockTags.DIRT) && Blocks.DIRT == blockState.getBlock());
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            return simplePlacement(world, pos, blockState, null, null);
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            if (complete)
            {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return Collections.singletonList(new ItemStack(Blocks.DIRT));
        }
    }

    public static class DoorPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof DoorBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos,
            final RotationMirror settings)
        {
            if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
            {
                return simplePlacement(world, pos, blockState, settings, tileEntityData);
            }

            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
            {
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            }
            return itemList;
        }
    }

    public static class BedPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof BedBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.FOOT)
            {
                return simplePlacement(world, pos, blockState, settings, tileEntityData);
            }

            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                final List<ItemStack> list = new ArrayList<>();
                list.add(new ItemStack(blockState.getBlock(), 1));
                return list;
            }
            return Collections.emptyList();
        }
    }

    public static class DoublePlantPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof DoublePlantBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos,
            final RotationMirror settings)
        {
            if (blockState.getValue(DoublePlantBlock.HALF).equals(DoubleBlockHalf.LOWER))
            {
                return simplePlacement(world, pos, blockState, settings, tileEntityData);
            }
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            return itemList;
        }
    }

    public static class SpecialBlockPlacementAttemptHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof EndPortalBlock || blockState.getBlock() instanceof SpawnerBlock ||
                     blockState.getBlock() instanceof DragonEggBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            return new ArrayList<>();
        }
    }

    public static class FlowerPotPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof FlowerPotBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            return simplePlacement(world, pos, blockState, settings, tileEntityData);
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.add(new ItemStack(((FlowerPotBlock) blockState.getBlock()).getPotted()));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }
    }

    public static class AirPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof AirBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            if (!world.isEmptyBlock(pos))
            {
                final List<Entity> entityList =
                  world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> !(entity instanceof LivingEntity || entity instanceof ItemEntity));
                if (!entityList.isEmpty())
                {
                    for (final Entity entity : entityList)
                    {
                        entity.remove(Entity.RemovalReason.KILLED);
                    }
                }

                world.removeBlock(pos, false);
            }
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            return new ArrayList<>();
        }
    }

    public static class BlockGrassPathPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof DirtPathBlock;
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos,
            final RotationMirror settings)
        {
            return simplePlacement(world, pos, blockState, settings, tileEntityData);
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            if (complete)
            {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return Collections.singletonList(new ItemStack(Blocks.DIRT));
        }
    }

    public static class StairBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof StairBlock
                     && !(blockState.getBlock() instanceof EntityBlock)
                     && world.getBlockState(pos).getBlock() instanceof StairBlock
                     && world.getBlockState(pos).getValue(StairBlock.FACING) == blockState.getValue(StairBlock.FACING)
                     && world.getBlockState(pos).getValue(StairBlock.HALF) == blockState.getValue(StairBlock.HALF)
                     && blockState.getBlock() == world.getBlockState(pos).getBlock();
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            if (complete)
            {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return new ArrayList<>();
        }
    }

    public static class GeneralBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return true;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                world.removeBlock(pos, false);
                handleBlockPlacement(world, pos, blockState, settings, tileEntityData);
                return ActionProcessingResult.PASS;
            }

            if (!handleBlockPlacement(world, pos, blockState, settings, tileEntityData))
            {
                return ActionProcessingResult.DENY;
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState, world));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }
    }

    public static class ContainerPlacementHandler implements IPlacementHandler
    {
        public static Set<Block> CONTAINERS = Set.of();

        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return CONTAINERS.contains(blockState.getBlock());
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            if (!handleBlockPlacement(world, pos, blockState))
            {
                return ActionProcessingResult.DENY;
            }

            try
            {
                // Try detecting inventory content.
                ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState, world);
            }
            catch (final Exception ex)
            {
                // If we can't load the inventory content of the TE, return early, don't fill TE data.
                return ActionProcessingResult.SUCCESS;
            }

            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, settings);
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.addAll(getItemsFromTileEntity(tileEntityData, blockState, world));

            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }
    }

    /**
     * mojang abusing lazyupdates, this modification always happens, but we need it now, not later
     */
    public static class HopperClientLagPlacementHandler extends ContainerPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof HopperBlock;
        }

        @Override
        public ActionProcessingResult handle(final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final boolean complete,
            final BlockPos centerPos)
        {
            final boolean flag = !world.hasNeighborSignal(pos);
            return super.handle(world,
                pos,
                flag != blockState.getValue(HopperBlock.ENABLED) ? blockState.setValue(HopperBlock.ENABLED, flag) : blockState,
                tileEntityData,
                complete,
                centerPos);
        }
    }

    public static class BannerPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof BannerBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                handleTileEntityPlacement(tileEntityData, world, pos, settings);
                return ActionProcessingResult.PASS;
            }

            if (!handleBlockPlacement(world, pos, blockState, settings, tileEntityData))
            {
                return ActionProcessingResult.DENY;
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState, world));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }
    }

    public static class DripStoneBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() == Blocks.POINTED_DRIPSTONE;
        }

        @Override
        public ActionProcessingResult handle(
          final Blueprint blueprint,
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos,
          final RotationMirror settings)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.PASS;
            }

            if (blockState.getValue(PointedDripstoneBlock.THICKNESS) != DripstoneThickness.TIP && blockState.getValue(PointedDripstoneBlock.THICKNESS) != DripstoneThickness.TIP_MERGE)
            {
                return ActionProcessingResult.PASS;
            }

            final Direction dir = blockState.getValue(PointedDripstoneBlock.TIP_DIRECTION).getOpposite();
            if (blockState.getValue(PointedDripstoneBlock.THICKNESS) == DripstoneThickness.TIP_MERGE)
            {
                placeDripStoneInDir(dir.getOpposite(), blueprint, pos.subtract(centerPos).offset(blueprint.getPrimaryBlockOffset()), pos, blockState, world);
                placeDripStoneInDir(dir, blueprint, pos.subtract(centerPos).offset(blueprint.getPrimaryBlockOffset()), pos, blockState, world);
                handleBlockPlacement(world, pos, blockState);

                // both direction.
                return ActionProcessingResult.SUCCESS;
            }

            placeDripStoneInDir(dir, blueprint, pos.subtract(centerPos).offset(blueprint.getPrimaryBlockOffset()), pos, blockState, world);
            handleBlockPlacement(world, pos, blockState);
            return ActionProcessingResult.SUCCESS;
        }

        private static void placeDripStoneInDir(final Direction dir, final Blueprint blueprint, final BlockPos blueprintPos, final BlockPos worldPos, final BlockState blockState, final Level world)
        {
            try
            {
                final BlockState firstState = blueprint.getBlockState(blueprintPos.relative(dir, 1));
                if (firstState != null && firstState.getBlock() == Blocks.POINTED_DRIPSTONE)
                {
                    final BlockState secondState = blueprint.getBlockState(blueprintPos.relative(dir, 2));
                    if (secondState != null && secondState.getBlock() == Blocks.POINTED_DRIPSTONE)
                    {
                        handleBlockPlacement(world, worldPos.relative(dir, 2), secondState);
                    }

                    handleBlockPlacement(world, worldPos.relative(dir, 1), firstState);
                }
            }
            catch (final Exception ex)
            {
                Log.getLogger().error("Problem placing dripstone. Dripstone might extend outside of the schematic.");
            }
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState, world));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }
    }

    public static class BlackListedBlockPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.is(ModTags.BLUEPRINT_BLACKLIST);
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete,
          final BlockPos centerPos)
        {

            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final boolean complete)
        {
            return Collections.emptyList();
        }
    }

    /**
     * Handles block placement.
     *
     * @param world      the world.
     * @param pos        the position.
     * @param blockState the block state of the block about to be placed.
     * @return true if successfully placed.
     */
    public static boolean handleBlockPlacement(
        final @NotNull Level world,
        final @NotNull BlockPos pos,
        final @NotNull BlockState blockState)
    {
        return handleBlockPlacement(world, pos, blockState, null, null);
    }

    /**
     * Handles block placement.
     *
     * @param world      the world.
     * @param pos        the position.
     * @param blockState the block state of the block about to be placed.
     * @param settings   the rotation and mirroring settings.
     * @return true if successfully placed.
     */
    public static boolean handleBlockPlacement(
        final @NotNull Level world,
        final @NotNull BlockPos pos,
        final @NotNull BlockState blockState,
        final @Nullable RotationMirror settings,
        final @Nullable CompoundTag tileEntityData)
    {
        return handleBlockPlacement(LevelWriter::setBlock, world, pos, blockState, UPDATE_FLAG, settings, tileEntityData);
    }

    /**
     * Handles block placement.
     *
     * @param world      the world.
     * @param pos        the position.
     * @param blockState the block state of the block about to be placed.
     * @param settings   the rotation and mirroring settings.
     * @return true if successfully placed.
     */
    public static boolean handleBlockPlacement(
        final @NotNull BlockPlacementFunction placementFunction,
        final @NotNull Level world,
        final @NotNull BlockPos pos,
        final @NotNull BlockState blockState,
        final int flags,
        final @Nullable RotationMirror settings,
        final @Nullable CompoundTag tileEntityData)
    {
        final boolean success = placementFunction.execute(world, pos, blockState, flags);
        if (success)
        {
            if (tileEntityData != null && settings != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, settings);
            }

            blockState.getBlock().setPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
        }
        return success;
    }

    /**
     * Handler for placing the block down into the world.
     */
    public interface BlockPlacementFunction
    {
        boolean execute(LevelAccessor world, BlockPos pos, BlockState state, int flags);
    }

    /**
     * Handles tileEntity placement.
     *
     * @param tileEntityData the data of the tile entity.
     * @param world          the world.
     * @param pos            the position.
     * @param settings       the placement settings.
     */
    public static void handleTileEntityPlacement(@Nullable final CompoundTag tileEntityData, final Level world, final BlockPos pos, final RotationMirror settings)
    {
        if (tileEntityData != null)
        {
            final BlockEntity newTile = BlockEntity.loadStatic(pos, world.getBlockState(pos), tileEntityData, world.registryAccess());
            if (newTile != null)
            {
                if (newTile instanceof final IRotatableBlockEntity rotatable)
                {
                    rotatable.rotateAndMirror(settings);
                }

                final BlockEntity worldBlockEntity = world.getBlockEntity(pos);
                if (worldBlockEntity != null)
                {
                    worldBlockEntity.loadWithComponents(newTile.saveWithFullMetadata(world.registryAccess()), world.registryAccess());
                    worldBlockEntity.setChanged();
                }
                else
                {
                    world.setBlockEntity(newTile);
                }
                world.getBlockState(pos).mirror(settings.mirror());
                world.getBlockState(pos).rotate(world, pos, settings.rotation());
            }
        }
    }

    /**
     * Get the required items for this state.
     * @param world the world it will be placed in.
     * @param pos the pos to place it at.
     * @param state the state to place.
     * @param data its TE data.
     * @param complete if complete.
     * @return the required items.
     */
    public static List<ItemStack> getRequiredItemsForState(final Level world, final BlockPos pos, final BlockState state, final CompoundTag data, final boolean complete)
    {
        final IPlacementHandler placementHandler = getHandler(world, pos, state);
        return placementHandler.getRequiredItems(world, pos, state, data, complete);
    }

    /**
     * Gets the list of items from a possible tileEntity.
     *
     * @param tileEntityData the data.
     * @param blockState     the block.
     * @return the required list.
     */
    public static List<ItemStack> getItemsFromTileEntity(final CompoundTag tileEntityData, final BlockState blockState, final Level provider)
    {
        if (tileEntityData == null)
        {
            return Collections.emptyList();
        }
        try
        {
            return ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState, provider);
        }
        catch (final Exception ex)
        {
            // We might not be able to query all inventories like this.
            return Collections.emptyList();
        }
    }

    /**
     * Simple block placement that only overwrites the block if the same block is not already present.
     * If the block is already present, the tile entity data is updated if necessary.
     * If the block is not present, the block is placed down and the tile entity is set down too.
     *
     * @param world          receives the world.
     * @param pos            the position.
     * @param blockState     the blockState.
     * @param settings       the settings to use to rotate or mirror it.
     * @param tileEntityData the placer of the block.
     * @return ACCEPT, DENY or IGNORE.
     */
    public static IPlacementHandler.ActionProcessingResult simplePlacement(
        final @NotNull Level world,
        final @NotNull BlockPos pos,
        final @NotNull BlockState blockState,
        final @Nullable RotationMirror settings,
        final @Nullable CompoundTag tileEntityData)
    {
        if (world.getBlockState(pos).is(blockState.getBlock()))
        {
            handleTileEntityPlacement(tileEntityData, world, pos, settings);
            return IPlacementHandler.ActionProcessingResult.PASS;
        }
        if (!handleBlockPlacement(world, pos, blockState, settings, tileEntityData))
        {
            return IPlacementHandler.ActionProcessingResult.DENY;
        }

        return IPlacementHandler.ActionProcessingResult.SUCCESS;
    }
}
