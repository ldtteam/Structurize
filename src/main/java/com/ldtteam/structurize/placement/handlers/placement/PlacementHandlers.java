package com.ldtteam.structurize.placement.handlers.placement;

import com.ldtteam.structurize.api.util.IRotatableBlockEntity;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.IPlacementContext;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.tag.ModTags;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG;

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

    public enum AddType {
        BEFORE,
        AFTER,
        REPLACE
    }

    static
    {
        handlers.add(new AirPlacementHandler());
        handlers.add(new SolidSubstitutionPlacementHandler());
        handlers.add(new SubstitutionPlacementHandler());
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
        handlers.add(new HopperClientLagPlacementHandler());
        handlers.add(new DripStoneBlockPlacementHandler());
        handlers.add(new FallingBlockPlacementHandler());
        handlers.add(new BannerPlacementHandler());
        handlers.add(new DoBlockPlacementHandler());
        handlers.add(new DoDoorBlockPlacementHandler());
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
        synchronized (handlers)
        {
            for (int i = 0; i < handlers.size(); i++)
            {
                if (override == handlers.get(i).getClass())
                {
                    handlers.set(i, handler);
                    return;
                }
            }
            add(handler);
        }
    }

    /**
     * Allows for Adding a Handler before, after or instead of another one.
     * @param handler the new handler to add
     * @param override the class to match.
     * @param addType if before/after/replace.
     */
    public static void add(IPlacementHandler handler, Class<?> override, final AddType addType)
    {
        synchronized (handlers)
        {
            for (int i = 0; i < handlers.size(); i++)
            {
                if (override == handlers.get(i).getClass())
                {
                    switch (addType)
                    {
                        case BEFORE:
                            handlers.add(i, handler);
                            break;
                        case AFTER:
                            handlers.add(i+1, handler);
                            break;
                        case REPLACE:
                            handlers.set(i, handler);
                            break;
                    }
                    return;
                }
            }
            add(handler);
        }
    }

    /**
     * Adds a handler to the start of the handlers list, right after the air, solid and light placeholder handlers.
     * This may effectively override existing ones with similar 'canHandle' functions because this one will evaluate before them.
     * @param handler
     */
    public static void add(IPlacementHandler handler)
    {
        synchronized (handlers)
        {
            handlers.add(3, handler);
            handlerCache.clear();
        }
    }

    /**
     * Simple block based handler cache to avoid too many iterations
     */
    private static Map<Block, IPlacementHandler> handlerCache = new IdentityHashMap<>(128);

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
        final Block block = newState.getBlock();
        final IPlacementHandler cached = handlerCache.get(block);
        if (cached != null)
        {
            return cached;
        }

        for (final IPlacementHandler placementHandler : handlers)
        {
            if (placementHandler.canHandle(world, worldPos, newState))
            {
                handlerCache.put(block, placementHandler);
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
            return blockState.getBlock() == ModBlocks.blockFluidSubstitution.get();
        }

        @Override
        public List<ItemStack> getRequiredItems(
          Level world,
          BlockPos pos,
          BlockState blockState,
          @Nullable CompoundTag tileEntityData,
          IPlacementContext placementContext)
        {
            List<ItemStack> items = new ArrayList<>();

            if (!placementContext.fancyPlacement())
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
          IPlacementContext placementContext)
        {
            if (!placementContext.fancyPlacement())
            {
                world.setBlock(pos, ModBlocks.blockFluidSubstitution.get().defaultBlockState(), UPDATE_FLAG);
                return ActionProcessingResult.PASS;
            }

            if (world.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED))
            {
                world.setBlock(pos, world.getBlockState(pos).setValue(BlockStateProperties.WATERLOGGED, true), UPDATE_FLAG);
            }
            else
            {
                world.setBlock(pos, BlockUtils.getFluidForDimension(world), UPDATE_FLAG);
            }

            return ActionProcessingResult.PASS;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            // If is source keep, if water logged, keep, if full solid, keep. (Replace primarily if air, plant, etc).
            return worldState.getFluidState().isSource()
                || worldState.hasProperty(BlockStateProperties.WATERLOGGED)
                || BlockUtils.isAnySolid(worldState);
        }
    }

    public static class FirePlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof FireBlock;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
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
          final IPlacementContext placementContext)
        {
            world.setBlock(pos, blockState, UPDATE_FLAG);
            return ActionProcessingResult.PASS;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
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
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState));
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
                    itemList.addAll(getRequiredItemsForState(world, pos, supportBlockState, tileEntityData, placementContext));
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
          final IPlacementContext placementContext)
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
                world.setBlock(pos.below(), supportBlockState, UPDATE_FLAG);
            }
            if (!world.setBlock(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
        }
    }

    public static class GrassPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() == Blocks.GRASS_BLOCK;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            if (!world.setBlock(pos, blockState, UPDATE_FLAG))
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
          final IPlacementContext placementContext)
        {
            if (!placementContext.fancyPlacement())
            {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }
            return Collections.singletonList(new ItemStack(Blocks.DIRT));
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext placementContext)
        {
            if (placementContext.fancyPlacement())
            {
                return worldState.is(BlockTags.DIRT);
            }
            return worldState.equals(blueprintState);
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
          final IPlacementContext placementContext)
        {
            if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
            {
                world.setBlock(pos, blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlock(pos.above(), blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), UPDATE_FLAG);
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            if (blockState.getValue(DoorBlock.HALF).equals(DoubleBlockHalf.LOWER))
            {
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            }
            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            if (worldState.getBlock() == blueprintState.getBlock())
            {
                if (structureHandler.fancyPlacement())
                {
                    for (Property<?> property : worldState.getProperties())
                    {
                        // Compare properties, but if just open or powered don't match, ignore.
                        if (!blueprintState.hasProperty(property) ||
                            (blueprintState.getValue(property) != worldState.getValue(property)
                                && property != net.minecraft.world.level.block.DoorBlock.OPEN)
                                && property != net.minecraft.world.level.block.DoorBlock.POWERED)
                        {
                            return false;
                        }
                    }
                }
                else
                {
                    return worldState.equals(blueprintState);
                }
                return true;
            }
            return false;
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
          final IPlacementContext placementContext)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                final Direction facing = blockState.getValue(BedBlock.FACING);

                // pos.offset(facing) will get the other part of the bed
                world.setBlock(pos.relative(facing.getOpposite()), blockState.setValue(BedBlock.PART, BedPart.FOOT), UPDATE_FLAG);
                world.setBlock(pos, blockState.setValue(BedBlock.PART, BedPart.HEAD), UPDATE_FLAG);

                if (tileEntityData != null)
                {
                    handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
                    handleTileEntityPlacement(tileEntityData, world, pos.relative(facing.getOpposite()), placementContext.getRotationMirror());
                }
                return ActionProcessingResult.SUCCESS;
            }

            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                final List<ItemStack> list = new ArrayList<>();
                list.add(new ItemStack(blockState.getBlock(), 1));
                return list;
            }
            return Collections.emptyList();
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
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
          final IPlacementContext placementContext)
        {
            if (blockState.getValue(DoublePlantBlock.HALF).equals(DoubleBlockHalf.LOWER))
            {
                world.setBlock(pos, blockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), UPDATE_FLAG);
                world.setBlock(pos.above(), blockState.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), UPDATE_FLAG);
                return ActionProcessingResult.SUCCESS;
            }
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
        }
    }

    public static class SpecialBlockPlacementAttemptHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof EndPortalBlock
                || blockState.getBlock() instanceof SpawnerBlock
                || blockState.getBlock() instanceof DragonEggBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            return new ArrayList<>();
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return true;
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
          final IPlacementContext placementContext)
        {
            if (world.getBlockState(pos).getBlock() == Blocks.FLOWER_POT)
            {
                world.removeBlock(pos, false);
            }

            if (!world.setBlock(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
            }
            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            if (world.getBlockState(pos).getBlock() != Blocks.FLOWER_POT)
            {
                itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            }
            itemList.add(new ItemStack(((FlowerPotBlock) blockState.getBlock()).getContent()));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState)
                && blueprintState.getBlock() instanceof FlowerPotBlock blueprintPot
                && worldState.getBlock() instanceof FlowerPotBlock worldPot
                && blueprintPot.getContent() == worldPot.getContent();
        }
    }

    public static class AirPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.is(Blocks.AIR);
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
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
          final IPlacementContext placementContext)
        {
            return new ArrayList<>();
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.is(Blocks.AIR);
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
          final IPlacementContext placementContext)
        {
            if (!world.setBlock(pos, Blocks.DIRT_PATH.defaultBlockState(), UPDATE_FLAG))
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
         final IPlacementContext placementContext)
        {
            if (!placementContext.fancyPlacement())
            {
                return Collections.singletonList(new ItemStack(blockState.getBlock()));
            }

            // It's free, if the world block is already dirt.
            if (world.getBlockState(pos).is(Blocks.DIRT))
            {
                return Collections.emptyList();
            }
            return Collections.singletonList(new ItemStack(Blocks.DIRT));
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
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
          final IPlacementContext placementContext)
        {
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

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
        }
    }

    public static class ContainerPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() instanceof BaseEntityBlock;
        }

        @Override
        public ActionProcessingResult handle(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            if (!world.setBlock(pos, blockState, UPDATE_FLAG))
            {
                return ActionProcessingResult.DENY;
            }

            try
            {
                // Try detecting inventory content.
                ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState);
            }
            catch (final Exception ex)
            {
                // If we can't load the inventory content of the TE, return early, don't fill TE data.
                return ActionProcessingResult.SUCCESS;
            }

            if (tileEntityData != null)
            {
                handleTileEntityPlacement(tileEntityData, world, pos, placementContext.getRotationMirror());
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>();
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.addAll(getItemsFromTileEntity(tileEntityData, blockState));

            itemList.removeIf(ItemStackUtils::isEmpty);

            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.equals(blueprintState);
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
            final IPlacementContext placementContext)
        {
            final boolean flag = !world.hasNeighborSignal(pos);
            return super.handle(world,
                pos,
                flag != blockState.getValue(HopperBlock.ENABLED) ? blockState.setValue(HopperBlock.ENABLED, flag) : blockState,
                tileEntityData,
                placementContext);
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
          final IPlacementContext placementContext)
        {
            if (world.getBlockState(pos).equals(blockState))
            {
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
                blockState.getBlock().setPlacedBy(world, pos, blockState, null, BlockUtils.getItemStackFromBlockState(blockState));
            }

            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            // Always enter banner handling logic to make sure they're updated.
            return false;
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
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            final BlockPos centerPos = placementContext.getCenterPos();
            final Blueprint blueprint = placementContext.getBluePrint();
            if (world.getBlockState(pos).equals(blockState))
            {
                return ActionProcessingResult.PASS;
            }

            if (blueprint == null)
            {
                world.setBlock(pos, blockState, UPDATE_FLAG);
                return ActionProcessingResult.SUCCESS;
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
                world.setBlock(pos, blockState, UPDATE_FLAG);

                // both direction.
                return ActionProcessingResult.SUCCESS;
            }

            placeDripStoneInDir(dir, blueprint, pos.subtract(centerPos).offset(blueprint.getPrimaryBlockOffset()), pos, blockState, world);
            world.setBlock(pos, blockState, UPDATE_FLAG);
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
                        world.setBlock(worldPos.relative(dir, 2), secondState, UPDATE_FLAG);
                    }

                    world.setBlock(worldPos.relative(dir, 1), firstState, UPDATE_FLAG);
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
          final IPlacementContext placementContext)
        {
            final List<ItemStack> itemList = new ArrayList<>(getItemsFromTileEntity(tileEntityData, blockState));
            itemList.add(BlockUtils.getItemStackFromBlockState(blockState));
            itemList.removeIf(ItemStackUtils::isEmpty);
            return itemList;
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return worldState.getBlock() == blueprintState.getBlock();
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
            final IPlacementContext placementContext)
        {
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
          final Level world,
          final BlockPos pos,
          final BlockState blockState,
          @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            return Collections.emptyList();
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final @NotNull IPlacementContext structureHandler)
        {
            return true;
        }
    }

    public static class SolidSubstitutionPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() == ModBlocks.blockSolidSubstitution.get();
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            BlockState stateToPlace = blockState;
            if (placementContext.fancyPlacement())
            {
                stateToPlace = placementContext.getSolidBlockForPos(pos, placementContext.getBluePrint().getRawBlockStateFunction());
            }
            if (!world.setBlock(pos, stateToPlace, UPDATE_FLAG))
            {
                return ActionProcessingResult.PASS;
            }
            return ActionProcessingResult.SUCCESS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            if (placementContext.fancyPlacement())
            {
                return Collections.singletonList(BlockUtils.getItemStackFromBlockState(placementContext.getSolidBlockForPos(pos, placementContext.getBluePrint().getRawBlockStateFunction())));
            }
            else
            {
                return Collections.singletonList(new ItemStack(ModBlocks.blockSolidSubstitution.get()));
            }
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final IPlacementContext placementContext)
        {
            return worldState.equals(blueprintState) || (placementContext.fancyPlacement() && BlockUtils.isGoodFloorBlock(worldState));
        }
    }

    public static class SubstitutionPlacementHandler implements IPlacementHandler
    {
        @Override
        public boolean canHandle(final Level world, final BlockPos pos, final BlockState blockState)
        {
            return blockState.getBlock() == ModBlocks.blockSubstitution.get();
        }

        @Override
        public ActionProcessingResult handle(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            if (!placementContext.fancyPlacement())
            {
                world.setBlock(pos, blockState, UPDATE_FLAG);
            }
            return ActionProcessingResult.PASS;
        }

        @Override
        public List<ItemStack> getRequiredItems(
            final Level world,
            final BlockPos pos,
            final BlockState blockState,
            @Nullable final CompoundTag tileEntityData,
            final IPlacementContext placementContext)
        {
            if (placementContext.fancyPlacement())
            {
                return Collections.emptyList();
            }
            else
            {
                return Collections.singletonList(new ItemStack(ModBlocks.blockSubstitution.get()));
            }
        }

        @Override
        public boolean doesWorldStateMatchBlueprintState(
            final BlockState worldState,
            final BlockState blueprintState,
            final Tuple<BlockEntity, CompoundTag> blockEntityData,
            final IPlacementContext placementContext)
        {
            return placementContext.fancyPlacement() || worldState.equals(blueprintState);
        }
    }

    /**
     * Handles tileEntity placement.
     *
     * @param tileEntityData the data of the tile entity.
     * @param world          the world.
     * @param pos            the position.
     * @param settings       the placement settings.
     */
    public static void handleTileEntityPlacement(
      final CompoundTag tileEntityData,
      final Level world,
      final BlockPos pos,
      final PlacementSettings settings)
    {
        if (tileEntityData != null)
        {
            final BlockEntity newTile = BlockEntity.loadStatic(pos, world.getBlockState(pos), tileEntityData);
            if (newTile != null)
            {
                if (newTile instanceof IRotatableBlockEntity)
                {
                    ((IRotatableBlockEntity) newTile).rotate(settings.rotation);
                    ((IRotatableBlockEntity) newTile).mirror(settings.mirror);
                }

                final BlockEntity worldBlockEntity = world.getBlockEntity(pos);
                if (worldBlockEntity != null)
                {
                    worldBlockEntity.load(newTile.saveWithFullMetadata());
                    worldBlockEntity.setChanged();
                }
                else
                {
                    world.setBlockEntity(newTile);
                }
                world.getBlockState(pos).rotate(world, pos, settings.rotation);
                world.getBlockState(pos).mirror(settings.mirror);
            }
        }
    }

    /**
     * Get the required items for this state.
     * @param world the world it will be placed in.
     * @param pos the pos to place it at.
     * @param state the state to place.
     * @param data its TE data.
     * @param placementContext the placement context.
     * @return the required items.
     */
    public static List<ItemStack> getRequiredItemsForState(final Level world, final BlockPos pos, final BlockState state, final CompoundTag data, IPlacementContext placementContext)
    {
        final IPlacementHandler placementHandler = getHandler(world, pos, state);
        return placementHandler.getRequiredItems(world, pos, state, data, placementContext);
    }

    /**
     * Handles tileEntity placement.
     *
     * @param tileEntityData the data of the tile entity.
     * @param world          the world.
     * @param pos            the position.
     */
    public static void handleTileEntityPlacement(final CompoundTag tileEntityData, final Level world, final BlockPos pos)
    {
        handleTileEntityPlacement(tileEntityData, world, pos, new PlacementSettings());
    }

    /**
     * Gets the list of items from a possible tileEntity.
     *
     * @param tileEntityData the data.
     * @param blockState     the block.
     * @return the required list.
     */
    public static List<ItemStack> getItemsFromTileEntity(final CompoundTag tileEntityData, final BlockState blockState)
    {
        if (tileEntityData == null)
        {
            return Collections.emptyList();
        }
        try
        {
            return ItemStackUtils.getItemStacksOfTileEntity(tileEntityData, blockState);
        }
        catch (final Exception ex)
        {
            // We might not be able to query all inventories like this.
            return Collections.emptyList();
        }
    }
}
