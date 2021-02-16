package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.types.WoodType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

/**
 * The paperwall block class defining the paperwall.
 */
public class BlockPaperWall extends PaneBlock
{
    /**
     * The variants for the paperwall.
     */
    public static final EnumProperty<WoodType> VARIANT = EnumProperty.create("variant", WoodType.class);

    /**
     * The hardness this block has.
     */
    private static final float                      BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float                      RESISTANCE     = 1F;

    public BlockPaperWall()
    {
        super(Properties.create(Material.GLASS).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
    }

    @Override
    public BlockState rotate(final BlockState state, final IWorld world, final BlockPos pos, final Rotation direction)
    {
        switch (direction)
        {
            case CLOCKWISE_180:
                return state.with(NORTH, state.get(SOUTH))
                         .with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH))
                         .with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90:
                return state.with(NORTH, state.get(EAST))
                         .with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST))
                         .with(WEST, state.get(NORTH));
            case CLOCKWISE_90:
                return state.with(NORTH, state.get(WEST))
                         .with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST))
                         .with(WEST, state.get(SOUTH));
            default:
                return state;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, VARIANT, WATERLOGGED);
    }
}
