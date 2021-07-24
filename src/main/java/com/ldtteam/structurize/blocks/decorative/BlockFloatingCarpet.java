package com.ldtteam.structurize.blocks.decorative;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.level.block.state.BlockBehaviour;

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
        super(BlockBehaviour.Properties.of(Material.CLOTH_DECORATION).strength(0.1F).sound(SoundType.WOOL));
        this.color = color;
    }

    public DyeColor getColor()
    {
        return color;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos position, CollisionContext context) {
        return SHAPE;
    }
}
