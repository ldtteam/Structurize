package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeDirectional;
import com.ldtteam.structurize.blocks.types.ShingleSlabType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Decorative block
 */
public class BlockShingleSlab extends AbstractBlockStructurizeDirectional<BlockShingleSlab>
{
    /**
     * The bounding box of the slab.
     */
    protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);

    /**
     * The variants of the shingle slab.
     */
    public static final EnumProperty<ShingleSlabType> VARIANT = EnumProperty.create("variant", ShingleSlabType.class);

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockshingleslab";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Amount of connections with other shingle slabs.
     */
    private static final int NO_CONNECTIONS    = 0;
    private static final int THREE_CONNECTIONS = 1;
    private static final int TWO_CONNECTIONS   = 2;
    private static final int ONE_CONNECTION    = 3;

    /**
     * Constructor for the TimberFrame
     */
    public BlockShingleSlab()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }

    @Override
    public BlockState updatePostPlacement(final BlockState stateIn, final Direction HORIZONTAL_FACING, final BlockState HORIZONTAL_FACINGState, final IWorld worldIn, final BlockPos currentPos, final BlockPos HORIZONTAL_FACINGPos)
    {
        return getSlabShape(stateIn, worldIn, currentPos);
    }

    @Override
    public BlockState getStateForPlacement(
      final BlockState state,
      final Direction HORIZONTAL_FACING,
      final BlockState state2,
      final IWorld world,
      final BlockPos pos1,
      final BlockPos pos2,
      final Hand hand)
    {
        return getSlabShape(state, world, pos1);
    }

    /**
     * Get the step shape of the slab
     * @param state the state.
     * @param world the world.
     * @param position the position.Re
     * @return the blockState to use.
     */
    private static BlockState getSlabShape(@NotNull final BlockState state, @NotNull final IWorld world, @NotNull final BlockPos position)
    {
        final boolean[] connectors = new boolean[]{!(world.getBlockState(position.east()).getBlock() instanceof BlockShingleSlab),
            !(world.getBlockState(position.west()).getBlock() instanceof BlockShingleSlab),
            !(world.getBlockState(position.north()).getBlock() instanceof BlockShingleSlab),
            !(world.getBlockState(position.south()).getBlock() instanceof BlockShingleSlab)};

        int amount = 0;
        for(final boolean check: connectors)
        {
            if(check)
            {
                amount++;
            }
        }

        if(amount == NO_CONNECTIONS)
        {
            return state.with(VARIANT, ShingleSlabType.TOP);
        }
        if(amount == THREE_CONNECTIONS)
        {
            if (connectors[0])
            {
                return state.with(VARIANT, ShingleSlabType.ONE_WAY).with(HORIZONTAL_FACING, Direction.SOUTH);
            }
            else if (connectors[1])
            {
                return state.with(VARIANT, ShingleSlabType.ONE_WAY).with(HORIZONTAL_FACING, Direction.NORTH);
            }
            else if (connectors[2])
            {
                return state.with(VARIANT, ShingleSlabType.ONE_WAY).with(HORIZONTAL_FACING, Direction.EAST);
            }
            return state.with(VARIANT, ShingleSlabType.ONE_WAY).with(HORIZONTAL_FACING, Direction.WEST);
        }
        else if(amount == TWO_CONNECTIONS)
        {
            if (connectors[0] && connectors[1] && !connectors[2] && !connectors[3])
            {
                return state.with(VARIANT, ShingleSlabType.TWO_WAY).with(HORIZONTAL_FACING, Direction.EAST);
            }
            else if (!connectors[0] && !connectors[1] && connectors[2] && connectors[3])
            {
                return state.with(VARIANT, ShingleSlabType.TWO_WAY).with(HORIZONTAL_FACING, Direction.NORTH);
            }
            else if(!connectors[0] && connectors[1] && connectors[2] && !connectors[3])
            {
                return state.with(VARIANT, ShingleSlabType.CURVED).with(HORIZONTAL_FACING, Direction.WEST);
            }
            else if(connectors[0] && !connectors[1] && !connectors[2] && connectors[3])
            {
                return state.with(VARIANT, ShingleSlabType.CURVED).with(HORIZONTAL_FACING, Direction.EAST);
            }
            else if(!connectors[0] && connectors[1] && !connectors[2] && connectors[3])
            {
                return state.with(VARIANT, ShingleSlabType.CURVED).with(HORIZONTAL_FACING, Direction.SOUTH);
            }
            return state.with(VARIANT, ShingleSlabType.CURVED).with(HORIZONTAL_FACING, Direction.NORTH);
        }
        else if(amount == ONE_CONNECTION)
        {
            if (!connectors[0] && !world.isAirBlock(position.west().down()))
            {
                return state.with(VARIANT, ShingleSlabType.THREE_WAY).with(HORIZONTAL_FACING, Direction.NORTH);
            }
            else if (!connectors[1] && !world.isAirBlock(position.east().down()))
            {
                return state.with(VARIANT, ShingleSlabType.THREE_WAY).with(HORIZONTAL_FACING, Direction.SOUTH);
            }
            else if (!connectors[2] && !world.isAirBlock(position.south().down()))
            {
                return state.with(VARIANT, ShingleSlabType.THREE_WAY).with(HORIZONTAL_FACING, Direction.WEST);
            }
            else if (!connectors[3] && !world.isAirBlock(position.north().down()))
            {
                return state.with(VARIANT, ShingleSlabType.THREE_WAY).with(HORIZONTAL_FACING, Direction.EAST);
            }
            return state.with(VARIANT, ShingleSlabType.TWO_WAY)
                    .with(HORIZONTAL_FACING, !connectors[0] || !connectors[1] ? Direction.NORTH : Direction.EAST);
        }
        return state.with(VARIANT, ShingleSlabType.FOUR_WAY);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, VARIANT);
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IEnviromentBlockReader world, final BlockPos pos, final Direction face)
    {
        return false;
    }
}
