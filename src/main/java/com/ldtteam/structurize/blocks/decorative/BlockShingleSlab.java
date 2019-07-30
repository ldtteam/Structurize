package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeDirectional;
import com.ldtteam.structurize.blocks.types.ShingleFaceType;
import com.ldtteam.structurize.blocks.types.ShingleSlabShapeType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.blocks.types.ShingleSlabShapeType.*;
import static net.minecraft.util.Direction.*;

/**
 * Decorative block
 */
public class BlockShingleSlab extends AbstractBlockStructurizeDirectional<BlockShingleSlab>
{
    /**
     * The SHAPEs of the shingle slab.
     */
    public static final EnumProperty<ShingleSlabShapeType> SHAPE = EnumProperty.create("shape", ShingleSlabShapeType.class);

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Amount of connections with other shingle slabs.
     */
    private static final int FOUR_CONNECTIONS = 4;
    private static final int THREE_CONNECTIONS = 3;
    private static final int TWO_CONNECTIONS = 2;
    private static final int ONE_CONNECTION = 1;

    /**
     * Registered ShingleFaceType for this block, used by the Data Generators.
     */
    private final ShingleFaceType faceType;

    /**
     * Constructor for the TimberFrame
     */
    public BlockShingleSlab(final ShingleFaceType faceType)
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(faceType.getName() + "_shingle_slab");
        this.faceType = faceType;
    }

    // Deprecated here just means that you should not use this method when referencing a block, and instead it's blockstate <- Forge's Discord
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
     * Get the registered ShingleFaceType, used by the Data Generators
     *
     * @return the registered ShingleFaceType
     */
    public ShingleFaceType getFaceType()
    {
        return this.faceType;
    }

    /**
     * Make the slab and actual slab shape.
     *
     * @param state Current block state.
     * @param worldIn The world the block is in.
     * @param pos The position of the block.
     * @param context The selection context.
     * @return The VoxelShape of the block.
     */
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context)
    {
        return Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    }

    /**
     * Get the step shape of the slab
     *
     * @param state    the state.
     * @param world    the world.
     * @param position the position.Re
     * @return the blockState to use.
     */
    private static BlockState getSlabShape(@NotNull final BlockState state, @NotNull final IWorld world, @NotNull final BlockPos position)
    {
        final boolean north = world.getBlockState(position.north()).getBlock() instanceof BlockShingleSlab;
        final boolean south = world.getBlockState(position.south()).getBlock() instanceof BlockShingleSlab;
        final boolean east = world.getBlockState(position.east()).getBlock() instanceof BlockShingleSlab;
        final boolean west = world.getBlockState(position.west()).getBlock() instanceof BlockShingleSlab;

        final boolean[] connectors = new boolean[]{north, south, east, west};

        int amount = 0;
        for (final boolean check : connectors)
        {
            if (check)
            {
                amount++;
            }
        }

        BlockState shapeState;
        if (amount == ONE_CONNECTION)
        {
            shapeState = state.with(SHAPE, ONE_WAY);
            if (north)
                return shapeState.with(HORIZONTAL_FACING, NORTH);
            if (south)
                return shapeState.with(HORIZONTAL_FACING, SOUTH);
            if (east)
                return shapeState.with(HORIZONTAL_FACING, EAST);
            if (west)
                return shapeState.with(HORIZONTAL_FACING, WEST);
        }

        if (amount == TWO_CONNECTIONS)
        {
            if (north && east)
            {
                shapeState = state.with(SHAPE, CURVED);
                return shapeState.with(HORIZONTAL_FACING, WEST);
            }
            if (north && west)
            {
                shapeState = state.with(SHAPE, CURVED);
                return shapeState.with(HORIZONTAL_FACING, SOUTH);
            }
            if (south && east)
            {
                shapeState = state.with(SHAPE, CURVED);
                return shapeState.with(HORIZONTAL_FACING, NORTH);
            }
            if (south && west)
            {
                shapeState = state.with(SHAPE, CURVED);
                return shapeState.with(HORIZONTAL_FACING, EAST);
            }
            if (north && south)
            {
                shapeState = state.with(SHAPE, TWO_WAY);
                return shapeState.with(HORIZONTAL_FACING, NORTH);
            }
            if (east && west)
            {
                shapeState = state.with(SHAPE, TWO_WAY);
                return shapeState.with(HORIZONTAL_FACING, EAST);
            }
        }

        if (amount == THREE_CONNECTIONS)
        {
            shapeState = state.with(SHAPE, THREE_WAY);
            if (north && east && west)
            {
                return shapeState.with(HORIZONTAL_FACING, NORTH);
            }
            if (south && east && west)
            {
                return shapeState.with(HORIZONTAL_FACING, SOUTH);
            }
            if (east && north && south)
            {
                return shapeState.with(HORIZONTAL_FACING, EAST);
            }
            if (west && north && south)
            {
                return shapeState.with(HORIZONTAL_FACING, WEST);
            }
        }

        if (amount == FOUR_CONNECTIONS)
        {
            shapeState = state.with(SHAPE, FOUR_WAY);
            return shapeState;
        }

        return state.with(SHAPE, TOP);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HORIZONTAL_FACING, SHAPE);
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IEnviromentBlockReader world, final BlockPos pos, final Direction face)
    {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
}
