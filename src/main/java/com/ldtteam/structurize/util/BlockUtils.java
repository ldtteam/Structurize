package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.BlockToItemMaps.MapEnum;
import net.minecraft.block.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.Property;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
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

        if (fancy)
        {
            if (structureBlock == ModBlocks.blockSubstitution.get() || worldState.equals(structureState))
            {
                return true;
            }

            if (structureBlock instanceof AirBlock && worldBlock instanceof AirBlock)
            {
                return true;
            }

            if (structureBlock == Blocks.DIRT && worldBlock.isIn(Tags.Blocks.DIRT))
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
        final Item item = stack.getItem();
        if (item instanceof AirItem)
        {
            return ((AirItem) item).block.getDefaultState();
        }
        else if (item instanceof BucketItem)
        {
            return ((BucketItem) item).getFluid().getDefaultState().getBlockState();
        }
        else if (item instanceof BlockItem)
        {
            return ((BlockItem) item).getBlock().getDefaultState();
        }

        return def;
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack from the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final BlockState blockState, final MapEnum mapEnum)
    {
        final Block block = blockState.getBlock();
        IItemProvider item = BlockToItemMaps.getFrom(block, mapEnum);
        item = item == null ? block : item;

        return new ItemStack(item, 1);
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

        if (item == Items.AIR)
        {
            world.removeBlock(here, false);
        }
        else if (item instanceof AirItem)
        {
            world.setBlockState(here, ((AirItem) item).block.getDefaultState(), Constants.UPDATE_FLAG);
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
     * A simple check to fetch the default fluid block for this dimension
     * @param world the world of the dimension
     * @return the default blockstate for the default fluid
     */
    public static BlockState getFluidForDimension(World world)
    {
        ResourceLocation res = world.func_241828_r().func_230520_a_().getKey(world.getDimensionType());
        if (res == null)
        {
            return Blocks.WATER.getDefaultState();
        }

        RegistryKey<DimensionType> rk = RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, res);

        return rk == DimensionType.THE_NETHER
                ? Blocks.LAVA.getDefaultState()
                : Blocks.WATER.getDefaultState();
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
        return target.getClass().equals(propertiesOrigin.getBlock().getClass())
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
