package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.types.WoodType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

/**
 * The paperwall block class defining the paperwall.
 */
public class BlockPaperWall extends IronBarsBlock
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
    public BlockState rotate(final BlockState state, final LevelAccessor world, final BlockPos pos, final Rotation direction)
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, VARIANT, WATERLOGGED);
    }

    public WoodType getType()
    {
        return type;
    }
}
