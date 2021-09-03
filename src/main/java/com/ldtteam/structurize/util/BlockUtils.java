package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.GameData;

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
        public static final List<BiPredicate<Block, BlockState>> FREE_TO_PLACE_BLOCKS = Arrays.asList(
        (block, iBlockState) -> block.equals(Blocks.AIR),
        (block, iBlockState) -> iBlockState.getMaterial().isLiquid(),
        (block, iBlockState) -> BlockUtils.isWater(block.defaultBlockState()),
        (block, iBlockState) -> block instanceof LeavesBlock,
        (block, iBlockState) -> block instanceof DoublePlantBlock,
        (block, iBlockState) -> block.equals(Blocks.GRASS),
        (block, iBlockState) -> block instanceof DoorBlock && iBlockState != null && iBlockState.getValue(BooleanProperty.create("upper")));

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
    public static BlockState getSubstitutionBlockAtWorld(final Level world, final BlockPos location)
    {
        final BlockState filler = world.getBiome(location).generationSettings.getSurfaceBuilderConfig().getTopMaterial();
        if (filler.getBlock() == Blocks.SAND)
        {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (filler.getBlock() instanceof FallingBlock)
        {
            return Blocks.DIRT.defaultBlockState();
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

    private static Item getItem(final BlockState blockState)
    {
        final Block block = blockState.getBlock();
        if (block.equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (block instanceof CropBlock)
        {
            final ItemStack stack = ((CropBlock) block).getCloneItemStack(null, null, blockState);
            if (stack != null)
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        // oh no... 
        else if (block instanceof FarmBlock || block instanceof DirtPathBlock)
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
     * @param structureState the first blockState.
     * @param worldState the second blockState.
     * @param shallReplace the not solid condition.
     * @param fancy if fancy paste.
     * @return true if nothing has to be done.
     */
    public static boolean areBlockStatesEqual(final BlockState structureState, final BlockState worldState, final Predicate<BlockState> shallReplace, final boolean fancy, final BiPredicate<BlockState, BlockState> specialEqualRule)
    {
        if (structureState == null || worldState == null)
        {
            return true;
        }

        final Block structureBlock = structureState.getBlock();
        final Block worldBlock = worldState.getBlock();

        if (worldState.equals(structureState))
        {
            return true;
        }

        if (fancy)
        {
            if (structureBlock == ModBlocks.blockSubstitution.get())
            {
                return true;
            }

            if (structureBlock instanceof AirBlock && worldBlock instanceof AirBlock)
            {
                return true;
            }

            if (structureBlock == Blocks.DIRT && worldState.is(Tags.Blocks.DIRT))
            {
                return true;
            }

            if (structureBlock == ModBlocks.blockSolidSubstitution.get() && !shallReplace.test(worldState))
            {
                return true;
            }

            // if the other block has fluid already or is not waterloggable, take no action
            if ((structureBlock == ModBlocks.blockFluidSubstitution.get()
                && (worldState.getFluidState().isSource()
                    || !worldState.hasProperty(BlockStateProperties.WATERLOGGED)
                    && worldState.getMaterial().isSolid()))
             || (worldBlock == ModBlocks.blockFluidSubstitution.get()
                && (structureState.getFluidState().isSource()
                    || !structureState.hasProperty(BlockStateProperties.WATERLOGGED)
                    && structureState.getMaterial().isSolid())))
            {
                return true;
            }
        }

        return specialEqualRule.test(structureState, worldState);
    }

    /**
     * Get a blockState from an itemStack.
     *
     * @param stack the stack to analyze.
     * @return the IBlockState.
     */
    public static BlockState getBlockStateFromStack(final ItemStack stack)
    {
        return getBlockStateFromStack(stack, Blocks.AIR.defaultBlockState());
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
            return Blocks.AIR.defaultBlockState();
        }
        else if (stack.getItem() instanceof BucketItem)
        {
            return ((BucketItem) stack.getItem()).getFluid().defaultFluidState().createLegacyBlock();
        }
        else if (stack.getItem() instanceof BlockItem)
        {
            return ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        }

        return def;
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(final BlockState blockState)
    {
        if (blockState.getBlock() instanceof LiquidBlock)
        {
            return new ItemStack(((LiquidBlock) blockState.getBlock()).getFluid().getBucket(), 1);
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
        final Level world,
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
            BlockState newState = copyFirstCommonBlockStateProperties(targetBlock.defaultBlockState(), blockState);

            if (newState == null)
            {
                fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, stackToPlace);
                if (stackToPlace.is(ItemTags.BEDS) && blockState.hasProperty(HorizontalDirectionalBlock.FACING))
                {
                    fakePlayer.setYRot(blockState.getValue(HorizontalDirectionalBlock.FACING).get2DDataValue() * 90);
                }

                newState = targetBlock.getStateForPlacement(new BlockPlaceContext(new UseOnContext(fakePlayer,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(new Vec3(0, 0, 0),
                        itemStack.getItem() instanceof BedItem ? Direction.UP : Direction.NORTH,
                        here,
                        true))));

                if (newState == null)
                {
                    return;
                }
            }

            // place
            world.removeBlock(here, false);
            world.setBlock(here, newState, Constants.UPDATE_FLAG);
            targetBlock.setPlacedBy(world, here, newState, fakePlayer, stackToPlace);
        }
        else if (item instanceof BucketItem)
        {
            final Block sourceBlock = blockState.getBlock();
            final BucketItem bucket = (BucketItem) item;
            final Fluid fluid = bucket.getFluid();

            // place
            if (sourceBlock instanceof final LiquidBlockContainer liquidContainer)
            {
                if (liquidContainer.canPlaceLiquid(world, here, blockState, fluid))
                {
                    liquidContainer.placeLiquid(world, here, blockState, fluid.defaultFluidState());
                    bucket.checkExtraContent(null, world, stackToPlace, here);
                }
            }
            else
            {
                world.removeBlock(here, false);
                world.setBlock(here, fluid.defaultFluidState().createLegacyBlock(), Constants.UPDATE_FLAG);
                bucket.checkExtraContent(null, world, stackToPlace, here);
            }
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
    public static void removeFluid(Level world, BlockPos pos)
    {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if((!(block instanceof BucketPickup) || ((BucketPickup)block).pickupBlock(world, pos, state).isEmpty()) && block instanceof LiquidBlock)
        {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.UPDATE_FLAG);
        }
    }

    /**
     * A simple check to fetch the default fluid block for this dimension
     * @param world the world of the dimension
     * @return the default blockstate for the default fluid
     */
    public static BlockState getFluidForDimension(Level world)
    {
        return world.dimensionType().ultraWarm()
                ? Blocks.LAVA.defaultBlockState()
                : Blocks.WATER.defaultBlockState();
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
    public static List<ItemStack> getBlockDrops(final Level world, final BlockPos coords, final int fortune, final ItemStack stack)
    {
        return world.getBlockState(coords).getDrops(new LootContext.Builder((ServerLevel) world)
                                                      .withLuck(fortune)
                                                      .withParameter(LootContextParams.ORIGIN, new Vec3(coords.getX(), coords.getY(), coords.getZ()))
                                                      .withOptionalParameter(LootContextParams.BLOCK_ENTITY, world.getBlockEntity(coords))
                                                      .withParameter(LootContextParams.TOOL, stack));
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
        return target.getClass().equals(propertiesOrigin.getBlock().getClass())
            ? unsafeCopyBlockStateProperties(target.defaultBlockState(), propertiesOrigin, propertiesOrigin.getProperties())
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
            blockState = blockState.setValue(property, propertiesOrigin.getValue(property));
        }
        return blockState;
    }
}
