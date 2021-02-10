package com.ldtteam.structurize.blocks.decorative;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class BlockFloatingCarpet extends Block
{
    /**
     * This Blocks shape.
     */
    protected static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    /**
     * This Blocks color.
     */
    private final DyeColor color;

    public BlockFloatingCarpet(final DyeColor color, final Properties properties)
    {
        super(properties);
        this.color = color;
    }

    public DyeColor getColor()
    {
        return color;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos position, ISelectionContext context) {
        return SHAPE;
    }
}
