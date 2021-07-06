package com.ldtteam.structurize.blocks.decorative;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import net.minecraft.block.AbstractBlock;

public class BlockFloatingCarpet extends Block
{
    /**
     * This Blocks shape.
     */
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    /**
     * This Blocks color.
     */
    private final DyeColor color;

    public BlockFloatingCarpet(final DyeColor color)
    {
        super(AbstractBlock.Properties.of(Material.CLOTH_DECORATION).strength(0.1F).sound(SoundType.WOOL));
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
