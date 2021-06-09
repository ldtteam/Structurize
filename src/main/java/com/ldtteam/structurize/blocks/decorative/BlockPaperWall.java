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

import net.minecraft.block.AbstractBlock.Properties;

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

    private final WoodType type;

    public BlockPaperWall(WoodType type)
    {
        super(Properties.of(Material.GLASS).strength(BLOCK_HARDNESS, RESISTANCE));
        this.type = type;
    }

    @Override
    public BlockState rotate(final BlockState state, final IWorld world, final BlockPos pos, final Rotation direction)
    {
        switch (direction)
        {
            case CLOCKWISE_180:
                return state.setValue(NORTH, state.getValue(SOUTH))
                         .setValue(EAST, state.getValue(WEST)).setValue(SOUTH, state.getValue(NORTH))
                         .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.setValue(NORTH, state.getValue(EAST))
                         .setValue(EAST, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(WEST))
                         .setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.setValue(NORTH, state.getValue(WEST))
                         .setValue(EAST, state.getValue(NORTH)).setValue(SOUTH, state.getValue(EAST))
                         .setValue(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, VARIANT, WATERLOGGED);
    }

    public WoodType getType()
    {
        return type;
    }
}
