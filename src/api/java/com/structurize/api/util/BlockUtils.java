package com.structurize.api.util;

import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class for all Block type checking.
 */
public final class BlockUtils
{
    /**
     * Predicated to determine if a block is free to place.
     */
    @NotNull
    private static final List<BiPredicate<Block, IBlockState>> freeToPlaceBlocks =
      Arrays.asList(
        (block, iBlockState) -> block.equals(Blocks.AIR),
        (block, iBlockState) -> iBlockState.getMaterial().isLiquid(),
        (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
        (block, iBlockState) -> block.equals(Blocks.LEAVES),
        (block, iBlockState) -> block.equals(Blocks.LEAVES2),
        (block, iBlockState) -> block.equals(Blocks.DOUBLE_PLANT),
        (block, iBlockState) -> block.equals(Blocks.GRASS),
        (block, iBlockState) -> block instanceof BlockDoor
                                  && iBlockState != null
                                  && iBlockState.getValue(PropertyBool.create("upper"))

      );

    /**
     * Private constructor to hide the public one.
     */
    private BlockUtils()
    {
        //Hides implicit constructor.
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
     * Get the filler block at a certain location.
     * If block follows gravity laws return dirt.
     *
     * @param world    the world the block is in.
     * @param location the location it is at.
     * @return the IBlockState of the filler block.
     */
    public static IBlockState getSubstitutionBlockAtWorld(@NotNull final World world, @NotNull final BlockPos location)
    {
        final IBlockState filler = world.getBiome(location).fillerBlock;
        if (filler.getBlock() instanceof BlockFalling)
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
    public static boolean isWater(final IBlockState iBlockState)
    {
        return Objects.equals(iBlockState, Blocks.WATER.getDefaultState())
                 || Objects.equals(iBlockState, Blocks.FLOWING_WATER.getDefaultState());
    }

    private static Item getItem(@NotNull final IBlockState blockState)
    {
        if (blockState.getBlock().equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (blockState.getBlock() instanceof BlockBanner)
        {
            return Items.BANNER;
        }
        else if (blockState.getBlock() instanceof BlockBed)
        {
            return Items.BED;
        }
        else if (blockState.getBlock() instanceof BlockBrewingStand)
        {
            return Items.BREWING_STAND;
        }
        else if (blockState.getBlock() instanceof BlockCake)
        {
            return Items.CAKE;
        }
        else if (blockState.getBlock() instanceof BlockCauldron)
        {
            return Items.CAULDRON;
        }
        else if (blockState.getBlock() instanceof BlockCocoa)
        {
            return Items.DYE;
        }
        else if (blockState.getBlock() instanceof BlockCrops)
        {
            final ItemStack stack = ((BlockCrops) blockState.getBlock()).getItem(null, null, blockState);
            if (stack != null)
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        else if (blockState.getBlock() instanceof BlockDaylightDetector)
        {
            return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
        }
        else if (blockState.getBlock() instanceof BlockDoor)
        {
            final Item item = blockState.getBlock() == Blocks.IRON_DOOR ? Items.IRON_DOOR
                                : (blockState.getBlock() == Blocks.SPRUCE_DOOR ? Items.SPRUCE_DOOR
                                     : (blockState.getBlock() == Blocks.BIRCH_DOOR ? Items.BIRCH_DOOR
                                          : (blockState.getBlock() == Blocks.JUNGLE_DOOR ? Items.JUNGLE_DOOR
                                               : (blockState.getBlock() == Blocks.ACACIA_DOOR ? Items.ACACIA_DOOR
                                                    : (blockState.getBlock() == Blocks.DARK_OAK_DOOR
                                                         ? Items.DARK_OAK_DOOR
                                                         : Items.OAK_DOOR)))));

            return item == null ? Item.getItemFromBlock(blockState.getBlock()) : item;
        }
        else if (blockState.getBlock() instanceof BlockFarmland || blockState.getBlock() instanceof BlockGrassPath)
        {
            return Item.getItemFromBlock(Blocks.DIRT);
        }
        else if (blockState.getBlock() instanceof BlockFire)
        {
            return Items.FLINT_AND_STEEL;
        }
        else if (blockState.getBlock() instanceof BlockFlowerPot)
        {
            return Items.FLOWER_POT;
        }
        else if (blockState.getBlock() instanceof BlockFurnace)
        {
            return Item.getItemFromBlock(Blocks.FURNACE);
        }
        else if (blockState.getBlock() instanceof BlockHugeMushroom)
        {
            // Can the builder even build this?
            return blockState.getBlock().getItemDropped(null, null, 0);
        }
        else if (blockState.getBlock() instanceof BlockNetherWart)
        {
            return Items.NETHER_WART;
        }
        else if (blockState.getBlock() instanceof BlockPistonExtension)
        {
            // Not really sure what we want to do here...
            return blockState.getValue(BlockPistonExtension.TYPE) == BlockPistonExtension.EnumPistonType.STICKY
                     ? Item.getItemFromBlock(Blocks.STICKY_PISTON)
                     : Item.getItemFromBlock(Blocks.PISTON);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneComparator)
        {
            return Items.COMPARATOR;
        }
        else if (blockState.getBlock() instanceof BlockRedstoneLight)
        {
            return Item.getItemFromBlock(Blocks.REDSTONE_LAMP);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneRepeater)
        {
            return Items.REPEATER;
        }
        else if (blockState.getBlock() instanceof BlockRedstoneTorch)
        {
            return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
        }
        else if (blockState.getBlock() instanceof BlockRedstoneWire)
        {
            return Items.REDSTONE;
        }
        else if (blockState.getBlock() instanceof BlockReed)
        {
            return Items.REEDS;
        }
        else if (blockState.getBlock() instanceof BlockSign)
        {
            return Items.SIGN;
        }
        else if (blockState.getBlock() instanceof BlockSkull)
        {
            return Items.SKULL;
        }
        else if (blockState.getBlock() instanceof BlockStem)
        {
            final ItemStack stack = ((BlockStem) blockState.getBlock()).getItem(null, null, blockState);
            if (!ItemStackUtils.isEmpty(stack))
            {
                return stack.getItem();
            }
            return Items.MELON_SEEDS;
        }
        else if (blockState.getBlock() instanceof BlockStoneSlab)
        {
            //Builder won't know how to build double stone slab
            return Item.getItemFromBlock(Blocks.STONE_SLAB);
        }
        else if (blockState.getBlock() instanceof BlockPurpurSlab)
        {
            return Item.getItemFromBlock(Blocks.PURPUR_SLAB);
        }
        else if (blockState.getBlock() instanceof BlockStoneSlabNew)
        {
            return Item.getItemFromBlock(Blocks.STONE_SLAB2);
        }
        else if (blockState.getBlock() instanceof BlockTripWire)
        {
            return Items.STRING;
        }
        else if (blockState.getBlock() instanceof BlockWoodSlab)
        {
            //Builder will also have trouble with double wood slab
            return Item.getItemFromBlock(Blocks.WOODEN_SLAB);
        }
        else
        {
            return GameData.getBlockItemMap().get(blockState.getBlock());
        }
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final IBlockState blockState)
    {
        if (blockState.getBlock() instanceof IFluidBlock)
        {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) blockState.getBlock()).getFluid(), 1000));
        }
        final Item item = getItem(blockState);

        if (item == null)
        {
            return null;
        }

        Block block = blockState.getBlock();
        if (item instanceof ItemBlock)
        {
            block = Block.getBlockFromItem(item);
        }

        return new ItemStack(item, 1, getDamageValue(block, blockState));
    }

    /**
     * Get the damage value from a block and blockState, where the block is the
     * placeable and obtainable block. The blockstate might differ from the
     * block.
     *
     * @param block      the block.
     * @param blockState the state.
     * @return the int damage value.
     */
    private static int getDamageValue(final Block block, @NotNull final IBlockState blockState)
    {
        if (block instanceof BlockFarmland || blockState.getBlock() instanceof BlockFarmland)
        {
            return 0;
        }
        if (block instanceof BlockCocoa)
        {
            return EnumDyeColor.BROWN.getDyeDamage();
        }
        else if (block instanceof BlockDirt)
        {
            if (blockState.getBlock() instanceof BlockGrassPath)
            {
                return Blocks.DIRT.getDefaultState().getValue(BlockDirt.VARIANT).getMetadata();
            }
            return blockState.getValue(BlockDirt.VARIANT).getMetadata();
        }
        else if (block instanceof BlockDoublePlant
                   && blockState.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER)
        {
            //If upper part we can't do much here
            return blockState.getValue(BlockDoublePlant.VARIANT).getMeta();
        }
        else if (block instanceof BlockNewLeaf)
        {
            return block.getMetaFromState(blockState) & 3;
        }
        else if (block instanceof BlockOre)
        {
            return 0;
        }
        else if (block instanceof BlockSilverfish || block instanceof BlockTallGrass)
        {
            return block.getMetaFromState(blockState);
        }
        else if (block instanceof BlockSlab)
        {
            return block.damageDropped(blockState) & 7;
        }
        else
        {
            //todo farmland doesn't have damage at all, sucker!
            return block.damageDropped(blockState);
        }
    }

    /**
     * Get a blockState from an itemStack.
     *
     * @param stack the stack to analyze.
     * @return the IBlockState.
     */
    public static IBlockState getBlockStateFromStack(final ItemStack stack)
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

        return stack.getItem() instanceof ItemBlock ? ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getItemDamage()) : Blocks.GOLD_BLOCK.getDefaultState();
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
    public static void handleCorrectBlockPlacement(final World world, final FakePlayer fakePlayer, final ItemStack itemStack, final IBlockState blockState, final BlockPos here)
    {
        final ItemStack stackToPlace = itemStack.copy();
        stackToPlace.setCount(stackToPlace.getMaxStackSize());
        fakePlayer.setHeldItem(EnumHand.MAIN_HAND, stackToPlace);

        if (itemStack.getItem() instanceof ItemBed)
        {
            fakePlayer.rotationYaw = blockState.getValue(BlockBed.FACING).getHorizontalIndex() * 90;
        }
        final EnumFacing facing = (itemStack.getItem() instanceof ItemDoor
                                     || itemStack.getItem() instanceof ItemBed
                                     || itemStack.getItem() instanceof ItemSlab) ? EnumFacing.UP : EnumFacing.NORTH;
        ForgeHooks.onPlaceItemIntoWorld(stackToPlace, fakePlayer, world, here, facing, 0, 0, 0, EnumHand.MAIN_HAND);

        final IBlockState newBlockState = world.getBlockState(here);
        if (newBlockState.getBlock() instanceof BlockStairs && blockState.getBlock() instanceof BlockStairs)
        {
            IBlockState transformation = newBlockState.withProperty(BlockStairs.FACING, blockState.getValue(BlockStairs.FACING));
            transformation = transformation.withProperty(BlockStairs.HALF, blockState.getValue(BlockStairs.HALF));
            transformation = transformation.withProperty(BlockStairs.SHAPE, blockState.getValue(BlockStairs.SHAPE));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockHorizontal && blockState.getBlock() instanceof BlockHorizontal
                   && !(blockState.getBlock() instanceof BlockBed))
        {
            final IBlockState transformation = newBlockState.withProperty(BlockHorizontal.FACING, blockState.getValue(BlockHorizontal.FACING));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockDirectional && blockState.getBlock() instanceof BlockDirectional)
        {
            final IBlockState transformation = newBlockState.withProperty(BlockDirectional.FACING, blockState.getValue(BlockDirectional.FACING));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockSlab && blockState.getBlock() instanceof BlockSlab)
        {
            final IBlockState transformation;
            if (blockState.getBlock() instanceof BlockDoubleStoneSlab || blockState.getBlock() instanceof BlockDoubleStoneSlabNew
                  || blockState.getBlock() instanceof BlockDoubleWoodSlab || blockState.getBlock() instanceof BlockPurpurSlab.Double)
            {
                if (newBlockState.getBlock() instanceof BlockWoodSlab)
                {
                    transformation = Blocks.DOUBLE_WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, newBlockState.getValue(BlockWoodSlab.VARIANT));
                }
                else if (newBlockState.getBlock() instanceof BlockStoneSlab)
                {
                    transformation = Blocks.DOUBLE_STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, newBlockState.getValue(BlockStoneSlab.VARIANT));
                }
                else if (newBlockState.getBlock() instanceof BlockPurpurSlab)
                {
                    transformation = Blocks.PURPUR_DOUBLE_SLAB.getDefaultState().withProperty(BlockPurpurSlab.VARIANT, newBlockState.getValue(BlockPurpurSlab.VARIANT));
                }
                else
                {
                    transformation = Blocks.DOUBLE_STONE_SLAB2.getDefaultState().withProperty(BlockStoneSlabNew.VARIANT, newBlockState.getValue(BlockStoneSlabNew.VARIANT));
                }

            }
            else
            {
                transformation = newBlockState.withProperty(BlockSlab.HALF, blockState.getValue(BlockSlab.HALF));
            }
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockLog && blockState.getBlock() instanceof BlockLog)
        {
            final IBlockState transformation = newBlockState.withProperty(BlockLog.LOG_AXIS, blockState.getValue(BlockLog.LOG_AXIS));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockRotatedPillar && blockState.getBlock() instanceof BlockRotatedPillar)
        {
            final IBlockState transformation = newBlockState.withProperty(BlockRotatedPillar.AXIS, blockState.getValue(BlockRotatedPillar.AXIS));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockTrapDoor && blockState.getBlock() instanceof BlockTrapDoor)
        {
            IBlockState transformation = newBlockState.withProperty(BlockTrapDoor.HALF, blockState.getValue(BlockTrapDoor.HALF));
            transformation = transformation.withProperty(BlockTrapDoor.FACING, blockState.getValue(BlockTrapDoor.FACING));
            transformation = transformation.withProperty(BlockTrapDoor.OPEN, blockState.getValue(BlockTrapDoor.OPEN));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof BlockDoor && blockState.getBlock() instanceof BlockDoor)
        {
            final IBlockState transformation = newBlockState.withProperty(BlockDoor.FACING, blockState.getValue(BlockDoor.FACING));
            world.setBlockState(here, transformation);
        }
        else if (stackToPlace.getItem() == Items.LAVA_BUCKET)
        {
            world.setBlockState(here, Blocks.LAVA.getDefaultState());
        }
        else if (stackToPlace.getItem() == Items.WATER_BUCKET)
        {
            world.setBlockState(here, Blocks.WATER.getDefaultState());
        }
    }

    /**
     * Compares two blocks and checks if they are equally dirt.
     * Meaning dirt and grass are equal. But podzol and coarse dirt not.
     *
     * @param structureBlock    the block of the structure.
     * @param worldBlock        the world block.
     * @param structureMetaData the structure metadata.
     * @param worldMetadata     the world metadata.
     * @return true if equal.
     */
    public static boolean isGrassOrDirt(
      @NotNull final Block structureBlock, @NotNull final Block worldBlock,
      @NotNull final IBlockState structureMetaData, @NotNull final IBlockState worldMetadata)
    {
        if ((structureBlock == Blocks.DIRT || structureBlock == Blocks.GRASS) && (worldBlock == Blocks.DIRT || worldBlock == Blocks.GRASS))
        {
            if (structureBlock == Blocks.DIRT
                  && (structureMetaData.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT
                        || structureMetaData.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL))
            {
                return false;
            }

            return worldBlock != Blocks.DIRT
                     || (worldMetadata.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.COARSE_DIRT
                           && worldMetadata.getValue(BlockDirt.VARIANT) != BlockDirt.DirtType.PODZOL);
        }
        return false;
    }
}
