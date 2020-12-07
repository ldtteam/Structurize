package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    public static final List<BiPredicate<Block, BlockState>> FREE_TO_PLACE_BLOCKS = Arrays.asList(
        (block, iBlockState) -> block.equals(Blocks.AIR),
        (block, iBlockState) -> iBlockState.getMaterial().isLiquid(),
        (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
        (block, iBlockState) -> block instanceof LeavesBlock,
        (block, iBlockState) -> block instanceof DoublePlantBlock,
        (block, iBlockState) -> block.equals(Blocks.GRASS),
        (block, iBlockState) -> block instanceof DoorBlock && iBlockState != null && iBlockState.get(BooleanProperty.create("upper")));

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
        final BlockState filler = world.getBiome(location).biomeGenerationSettings.getSurfaceBuilderConfig().getTop();
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

    private static Item getItem(@NotNull final BlockState blockState)
    {
        final Block block = blockState.getBlock();
        if (block.equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (block instanceof CropsBlock)
        {
            final ItemStack stack = ((CropsBlock) block).getItem(null, null, blockState);
            if (stack != null)
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        // oh no... 
        else if (block instanceof FarmlandBlock || block instanceof GrassPathBlock)
        {
            return getItemFromBlock(Blocks.DIRT);
        }
        else if (block instanceof FireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }
        else if (block instanceof FlowerPotBlock)
        {
            return Items.FLOWER_POT;
        }
        else if (block == Blocks.BAMBOO_SAPLING)
        {
            return Items.BAMBOO;
        }
        else
        {
            return getItemFromBlock(block);
        }
    }

    private static Item getItemFromBlock(final Block block)
    {
        return GameData.getBlockItemMap().get(block);
    }

    /**
     * For structure placement, check if two blocks are alike or if action has to be taken.
     * @param blockState1 the first blockState.
     * @param blockState2 the second blockState.
     * @param shallReplace the not solid condition.
     * @param fancy if fancy paste.
     * @return true if nothing has to be done.
     */
    public static boolean areBlockStatesEqual(final BlockState blockState1, final BlockState blockState2, final Predicate<BlockState> shallReplace, final boolean fancy, final BiPredicate<BlockState, BlockState> specialEqualRule)
    {
        if (blockState1 == null || blockState2 == null)
        {
            return true;
        }

        final Block block1 = blockState1.getBlock();
        final Block block2 = blockState2.getBlock();

        if (fancy)
        {
            if (block1 == ModBlocks.blockSubstitution || blockState2.equals(blockState1) || block2 == ModBlocks.blockSubstitution)
            {
                return true;
            }

            if ((block1 == ModBlocks.blockSolidSubstitution && !shallReplace.test(blockState2))
            || (block2 == ModBlocks.blockSolidSubstitution && !shallReplace.test(blockState1)))
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
        return getBlockStateFromStack(stack, Blocks.AIR.getDefaultState());
    }

    /**
     * Get a blockState from an itemStack.
     *
     * @param stack the stack to analyze.
     * @param def   default blockstate if stack is not transformable
     * @return the IBlockState.
     */
    public static BlockState getBlockStateFromStack(final ItemStack stack, final BlockState def)
    {
        if (stack.getItem() == Items.AIR)
        {
            return Blocks.AIR.getDefaultState();
        }
        else if (stack.getItem() instanceof BucketItem)
        {
            return ((BucketItem) stack.getItem()).getFluid().getDefaultState().getBlockState();
        }
        else if (stack.getItem() instanceof BlockItem)
        {
            return ((BlockItem) stack.getItem()).getBlock().getDefaultState();
        }

        return def;
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final BlockState blockState)
    {
        if (blockState.getBlock() instanceof FlowingFluidBlock)
        {
            return new ItemStack(((FlowingFluidBlock) blockState.getBlock()).getFluid().getFilledBucket(), 1);
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
        final Item item = stackToPlace.getItem();
        stackToPlace.setCount(stackToPlace.getMaxStackSize());

        if (item instanceof AirItem)
        {
            world.removeBlock(here, false);
        }
        else if (item instanceof BlockItem)
        {
            final Block targetBlock = ((BlockItem) item).getBlock();
            BlockState newState = copyFirstCommonBlockStateProperties(targetBlock.getDefaultState(), blockState);

            if (newState == null)
            {
                fakePlayer.setHeldItem(Hand.MAIN_HAND, stackToPlace);
                if (item.isIn(ItemTags.BEDS) && blockState.hasProperty(HorizontalBlock.HORIZONTAL_FACING))
                {
                    fakePlayer.rotationYaw = blockState.get(HorizontalBlock.HORIZONTAL_FACING).getHorizontalIndex() * 90;
                }

                newState = targetBlock.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(fakePlayer,
                    Hand.MAIN_HAND,
                    new BlockRayTraceResult(new Vector3d(0, 0, 0),
                        itemStack.getItem() instanceof BedItem ? Direction.UP : Direction.NORTH,
                        here,
                        true))));
            }

            // place
            world.removeBlock(here, false);
            world.setBlockState(here, newState, Constants.UPDATE_FLAG);
            targetBlock.onBlockPlacedBy(world, here, newState, fakePlayer, stackToPlace);
        }
        else if (item instanceof BucketItem)
        {
            final BucketItem bucket = (BucketItem) item;
            final Fluid fluid = bucket.getFluid();

            // place
            world.removeBlock(here, false);
            world.setBlockState(here, fluid.getDefaultState().getBlockState(), Constants.UPDATE_FLAG);
            bucket.onLiquidPlaced(world, stackToPlace, here);
        }
        else
        {
            throw new IllegalArgumentException(
                MessageFormat.format("Cannot handle placing of {0} instead of {1}?!", itemStack.toString(), blockState.toString()));
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
                                                      .withParameter(LootParameters.field_237457_g_, new Vector3d(coords.getX(), coords.getY(), coords.getZ()))
                                                      .withParameter(LootParameters.TOOL, stack));
    }

    /**
     * Copies property values from propertiesOrigin into new blockstate made from target block.
     *
     * @param target           properties destination
     * @param propertiesOrigin properties source
     * @return blockState of target block with properties of propertiesOrigin or null if both blocks are not the same class
     */
    public static BlockState copyBlockStateProperties(final Block target, final BlockState propertiesOrigin)
    {
        return target.getClass().isInstance(propertiesOrigin.getBlock())
            ? unsafeCopyBlockStateProperties(target.getDefaultState(), propertiesOrigin, propertiesOrigin.getProperties())
            : null;
    }

    /**
     * Copies property values from propertiesOrigin into new blockstate made from target Block.
     * If source and target are not the same block find the first common superclass and use its properties.
     *
     * @param target           properties destination
     * @param propertiesOrigin properties source
     * @return blockState of target block with properties of common super class or null if no common superclass found
     */
    public static BlockState copyFirstCommonBlockStateProperties(final BlockState target, final BlockState propertiesOrigin)
    {
        final BlockState sameClass = copyBlockStateProperties(target.getBlock(), propertiesOrigin);
        if (sameClass != null)
        {
            return sameClass;
        }

        final Class<?> firstCommonClass = JavaUtils.getFirstCommonSuperClass(target.getBlock().getClass(), propertiesOrigin.getBlock().getClass());
        if (firstCommonClass == Block.class || !Block.class.isAssignableFrom(firstCommonClass))
        {
            return null;
        }

        // It would be the best to get properties of firstCommonClass but since defaultstate is non-static and created in top level block classes
        // it's literally impossible to get them
        final Collection<Property<?>> properties = new ArrayList<>(target.getProperties());
        properties.retainAll(propertiesOrigin.getProperties());
        return unsafeCopyBlockStateProperties(target, propertiesOrigin, properties);
    }

    /**
     * Copies property values from properties using propertiesOrigin into new blockstate made from target Block.
     *
     * @param target           properties destination
     * @param propertiesOrigin properties source
     * @param properties       which properties to copy
     * @return blockState of target with given properties of propertiesOrigin
     * @throws IllegalArgumentException if target does not accept any of properties
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState unsafeCopyBlockStateProperties(final BlockState target,
        final BlockState propertiesOrigin,
        final Collection<Property<?>> properties)
    {
        BlockState blockState = target;
        for (final Property property : properties)
        {
            blockState = blockState.with(property, propertiesOrigin.get(property));
        }
        return blockState;
    }
}
