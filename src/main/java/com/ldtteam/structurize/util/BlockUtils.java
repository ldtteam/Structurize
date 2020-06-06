package com.ldtteam.structurize.util;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Utility class for all Block type checking.
 */
public final class BlockUtils
{
    /**
     * Predicated to determine if a block is free to place.
     */
    @NotNull
    public static final List<BiPredicate<Block, BlockState>> FREE_TO_PLACE_BLOCKS = Arrays.asList
    (
        (block, iBlockState) -> block.equals(Blocks.AIR),
        (block, iBlockState) -> iBlockState.getMaterial().isLiquid(),
        (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
        (block, iBlockState) -> block instanceof LeavesBlock,
        (block, iBlockState) -> block instanceof DoublePlantBlock,
        (block, iBlockState) -> block.equals(Blocks.GRASS),
        (block, iBlockState) -> block instanceof DoorBlock && iBlockState != null && iBlockState.get(BooleanProperty.create("upper"))
    );

    /**
     * Private constructor to hide the public one.
     */
    private BlockUtils()
    {
        // Hides implicit constructor.
    }

    /**
     * Updates the rotation of the structure depending on the input.
     *
     * @param rotation the rotation to be set.
     * @return returns the Rotation object.
     */
    public static Rotation getRotation(final int rotation)
    {
        switch (rotation)
        {
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }

    /**
     * Gets a rotation from a block facing.
     *
     * @param facing the block facing.
     * @return the int rotation.
     */
    public static int getRotationFromFacing(final Direction facing)
    {
        switch (facing)
        {
            case SOUTH:
                return 2;
            case EAST:
                return 1;
            case WEST:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Get the filler block at a certain location.
     * If block follows gravity laws return dirt.
     *
     * @param world    the world the block is in.
     * @param location the location it is at.
     * @return the IBlockState of the filler block.
     */
    public static BlockState getSubstitutionBlockAtWorld(@NotNull final World world, @NotNull final BlockPos location)
    {
        final BlockState filler = world.getBiome(location).getSurfaceBuilderConfig().getTop();
        if (filler.getBlock() == Blocks.SAND)
        {
            return Blocks.SANDSTONE.getDefaultState();
        }
        if (filler.getBlock() instanceof FallingBlock)
        {
            return Blocks.DIRT.getDefaultState();
        }
        return filler;
    }

    /**
     * Checks if the block is water.
     *
     * @param iBlockState block state to be checked.
     * @return true if is water.
     */
    public static boolean isWater(final BlockState iBlockState)
    {
        return iBlockState.getBlock() == Blocks.WATER;
    }

    private static Item getItem(@NotNull final BlockState forgeBlockState)
    {
        final BlockState blockState = forgeBlockState;
        if (blockState.getBlock().equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (blockState.getBlock() instanceof BrewingStandBlock)
        {
            return Items.BREWING_STAND;
        }
        else if (blockState.getBlock() instanceof CakeBlock)
        {
            return Items.CAKE;
        }
        else if (blockState.getBlock() instanceof CauldronBlock)
        {
            return Items.CAULDRON;
        }
        else if (blockState.getBlock() instanceof CocoaBlock)
        {
            return Items.COCOA_BEANS;
        }
        else if (blockState.getBlock() instanceof CropsBlock)
        {
            final ItemStack stack = ((CropsBlock) blockState.getBlock()).getItem(null, null, blockState);
            if (stack != null)
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        else if (blockState.getBlock() instanceof DaylightDetectorBlock)
        {
            return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
        }
        else if (blockState.getBlock() instanceof FarmlandBlock || blockState.getBlock() instanceof GrassPathBlock)
        {
            return Item.getItemFromBlock(Blocks.DIRT);
        }
        else if (blockState.getBlock() instanceof FireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }
        else if (blockState.getBlock() instanceof FlowerPotBlock)
        {
            return Items.FLOWER_POT;
        }
        else if (blockState.getBlock() instanceof FurnaceBlock)
        {
            return Item.getItemFromBlock(Blocks.FURNACE);
        }
        else if (blockState.getBlock() instanceof NetherWartBlock)
        {
            return Items.NETHER_WART;
        }
        else if (blockState.getBlock() instanceof RedstoneTorchBlock)
        {
            return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
        }
        else if (blockState.getBlock() instanceof RedstoneWireBlock)
        {
            return Items.REDSTONE;
        }
        else if (blockState.getBlock() instanceof SkullBlock)
        {
            return Items.SKELETON_SKULL;
        }
        else if (blockState.getBlock() instanceof StemBlock)
        {
            if (blockState.getBlock() == Blocks.PUMPKIN)
            {
                return Items.PUMPKIN_SEEDS;
            }
            else
            {
                return blockState.getBlock()== Blocks.MELON ? Items.MELON_SEEDS : null;
            }
        }
        else if (blockState.getBlock() == Blocks.BAMBOO_SAPLING)
        {
            return Items.BAMBOO;
        }
        else
        {
            return GameData.getBlockItemMap().get(blockState.getBlock());
        }
    }

    /**
     * For structure placement, check if two blocks are alike or if action has to be taken.
     * @param blockState1 the first blockState.
     * @param blockState2 the second blockState.
     * @param notSolid the not solid condition.
     * @param fancy if fancy paste.
     * @return true if nothing has to be done.
     */
    public static boolean areBlockStatesEqual(final BlockState blockState1, final BlockState blockState2, final Predicate<BlockState> notSolid, final boolean fancy, final BiPredicate<BlockState, BlockState> specialEqualRule)
    {
        if (blockState1 == null || blockState2 == null)
        {
            return true;
        }

        final Block block1 = blockState2.getBlock();
        final Block block2 = blockState1.getBlock();

        if (fancy)
        {
            if (block1 == ModBlocks.blockSubstitution || blockState2.equals(blockState1) || block2 == ModBlocks.blockSubstitution)
            {
                return true;
            }

            if ((block1 == ModBlocks.blockSolidSubstitution && !notSolid.test(blockState1))
            || (block2 == ModBlocks.blockSolidSubstitution && !notSolid.test(blockState2)))
            {
                return true;
            }
        }

        return specialEqualRule.test(blockState1, blockState2);
    }

    /**
     * Get a blockState from an itemStack.
     *
     * @param stack the stack to analyze.
     * @return the IBlockState.
     */
    public static BlockState getBlockStateFromStack(final ItemStack stack)
    {
        if (stack.getItem() == Items.AIR)
        {
            return Blocks.AIR.getDefaultState();
        }

        if (stack.getItem() == Items.WATER_BUCKET)
        {
            return Blocks.WATER.getDefaultState();
        }

        if (stack.getItem() == Items.LAVA_BUCKET)
        {
            return Blocks.LAVA.getDefaultState();
        }

        return stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock().getDefaultState() : Blocks.GOLD_BLOCK.getDefaultState();
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final BlockState blockState)
    {
        if (blockState.getBlock() instanceof IFluidBlock)
        {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) blockState.getBlock()).getFluid(), 1000));
        }
        final Item item = getItem(blockState);
        if (item != Items.AIR && item != null)
        {
            return new ItemStack(item, 1);
        }

        return new ItemStack(blockState.getBlock(), 1);
    }

    /**
     * Handle the placement of a specific block for a blockState at a certain position with a fakePlayer.
     *
     * @param world      the world object.
     * @param fakePlayer the fake player to place.
     * @param itemStack  the describing itemStack.
     * @param blockState the blockState in the world.
     * @param here       the position.
     */
    public static void handleCorrectBlockPlacement(
        final World world,
        final FakePlayer fakePlayer,
        final ItemStack itemStack,
        final BlockState blockState,
        final BlockPos here)
    {
        final ItemStack stackToPlace = itemStack.copy();
        stackToPlace.setCount(stackToPlace.getMaxStackSize());
        fakePlayer.setHeldItem(Hand.MAIN_HAND, stackToPlace);

        if (stackToPlace.getItem().isIn(ItemTags.BEDS) && blockState.has(HorizontalBlock.HORIZONTAL_FACING))
        {
            fakePlayer.rotationYaw = blockState.get(HorizontalBlock.HORIZONTAL_FACING).getHorizontalIndex() * 90;
        }

        final Direction facing = (itemStack.getItem() instanceof BedItem ? Direction.UP : Direction.NORTH);

        if (stackToPlace.getItem() instanceof BlockItem && !(((BlockItem) stackToPlace.getItem()).getBlock() instanceof AbstractButtonBlock))
        {
            world.setBlockState(here, ((BlockItem) stackToPlace.getItem()).getBlock().getStateForPlacement(new BlockItemUseContext(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(0, 0, 0), facing, here, true)))), Constants.BlockFlags.BLOCK_UPDATE);
        }
        else
        {
            world.removeBlock(here, false);
            ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(0, 0, 0), facing, here, true)));
        }

        final BlockState newBlockState = world.getBlockState(here);
        if (newBlockState.getBlock() instanceof AbstractButtonBlock && blockState.getBlock() instanceof AbstractButtonBlock)
        {
            BlockState transformation = newBlockState.with(AbstractButtonBlock.FACE, blockState.get(AbstractButtonBlock.FACE));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        if (newBlockState.getBlock() instanceof  FourWayBlock && blockState.getBlock() instanceof FourWayBlock)
        {
            BlockState transformation = newBlockState.with(FourWayBlock.EAST, blockState.get(FourWayBlock.EAST));
            transformation = transformation.with(FourWayBlock.NORTH, blockState.get(FourWayBlock.NORTH));
            transformation = transformation.with(FourWayBlock.WEST, blockState.get(FourWayBlock.WEST));
            transformation = transformation.with(FourWayBlock.SOUTH, blockState.get(FourWayBlock.SOUTH));
            transformation = transformation.with(FourWayBlock.WATERLOGGED, blockState.get(FourWayBlock.WATERLOGGED));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof SixWayBlock && blockState.getBlock() instanceof SixWayBlock)
        {
            BlockState transformation = newBlockState.with(SixWayBlock.EAST, blockState.get(SixWayBlock.EAST));
            transformation = transformation.with(SixWayBlock.NORTH, blockState.get(SixWayBlock.NORTH));
            transformation = transformation.with(SixWayBlock.WEST, blockState.get(SixWayBlock.WEST));
            transformation = transformation.with(SixWayBlock.SOUTH, blockState.get(SixWayBlock.SOUTH));
            transformation = transformation.with(SixWayBlock.UP, blockState.get(SixWayBlock.UP));
            transformation = transformation.with(SixWayBlock.DOWN, blockState.get(SixWayBlock.DOWN));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof BlockTimberFrame && blockState.getBlock() instanceof BlockTimberFrame)
        {
            final BlockState transformation = newBlockState.with(BlockTimberFrame.FACING, blockState.get(BlockTimberFrame.FACING));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof StairsBlock && blockState.getBlock() instanceof StairsBlock)
        {
            BlockState transformation = newBlockState.with(StairsBlock.FACING, blockState.get(StairsBlock.FACING));
            transformation = transformation.with(StairsBlock.HALF, blockState.get(StairsBlock.HALF));
            transformation = transformation.with(StairsBlock.SHAPE, blockState.get(StairsBlock.SHAPE));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof HorizontalBlock && blockState.getBlock() instanceof HorizontalBlock &&
            !(blockState.getBlock() instanceof BedBlock))
        {
            final BlockState transformation = newBlockState.with(HorizontalBlock.HORIZONTAL_FACING, blockState.get(HorizontalBlock.HORIZONTAL_FACING));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof DirectionalBlock && blockState.getBlock() instanceof DirectionalBlock)
        {
            final BlockState transformation = newBlockState.with(DirectionalBlock.FACING, blockState.get(DirectionalBlock.FACING));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof SlabBlock && blockState.getBlock() instanceof SlabBlock)
        {
            final BlockState transformation;
            transformation = newBlockState.with(SlabBlock.TYPE, blockState.get(SlabBlock.TYPE));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof LogBlock && blockState.getBlock() instanceof LogBlock)
        {
            final BlockState transformation = newBlockState.with(LogBlock.AXIS, blockState.get(LogBlock.AXIS));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof RotatedPillarBlock && blockState.getBlock() instanceof RotatedPillarBlock)
        {
            final BlockState transformation = newBlockState.with(RotatedPillarBlock.AXIS, blockState.get(RotatedPillarBlock.AXIS));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof TrapDoorBlock && blockState.getBlock() instanceof TrapDoorBlock)
        {
            BlockState transformation = newBlockState.with(TrapDoorBlock.HALF, blockState.get(TrapDoorBlock.HALF));
            transformation = transformation.with(TrapDoorBlock.HORIZONTAL_FACING, blockState.get(TrapDoorBlock.HORIZONTAL_FACING));
            transformation = transformation.with(TrapDoorBlock.OPEN, blockState.get(TrapDoorBlock.OPEN));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (newBlockState.getBlock() instanceof DoorBlock && blockState.getBlock() instanceof DoorBlock)
        {
            final BlockState transformation = newBlockState.with(DoorBlock.FACING, blockState.get(DoorBlock.FACING));
            world.setBlockState(here, transformation, Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (stackToPlace.getItem() == Items.LAVA_BUCKET)
        {
            world.setBlockState(here, Blocks.LAVA.getDefaultState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
        else if (stackToPlace.getItem() == Items.WATER_BUCKET)
        {
            world.setBlockState(here, Blocks.WATER.getDefaultState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
    }

    /**
     * Removes the fluid from the given position.
     * 
     * @param world the world to remove the fluid from.
     * @param pos   the position where to remove the fluid.
     */
    public static void removeFluid(World world, BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if((!(block instanceof IBucketPickupHandler) || ((IBucketPickupHandler)block).pickupFluid(world, pos, state) == Fluids.EMPTY) && block instanceof FlowingFluidBlock)
        {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), com.ldtteam.structurize.api.util.constant.Constants.UPDATE_FLAG);
        }
    }

    /**
     * Returns a list of drops possible mining a specific block with specific
     * fortune level.
     *
     * @param world   World the block is in.
     * @param coords  Coordinates of the block.
     * @param fortune Level of fortune on the pickaxe.
     * @param stack the tool.
     * @return List of {@link ItemStack} with possible drops.
     */
    public static List<ItemStack> getBlockDrops(@NotNull final World world, @NotNull final BlockPos coords, final int fortune, final ItemStack stack)
    {
        return world.getBlockState(coords).getDrops(new LootContext.Builder((ServerWorld) world)
                                                      .withLuck(fortune)
                                                      .withParameter(LootParameters.TOOL, stack)
                                                      .withParameter(LootParameters.POSITION, coords));
    }
}
