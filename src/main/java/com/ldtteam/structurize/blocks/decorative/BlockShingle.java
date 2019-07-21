package com.ldtteam.structurize.blocks.decorative;

import afu.org.checkerframework.checker.oigj.qual.O;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.NotNull;

/**
 * Class defining the general shingle.
 */
public class BlockShingle extends AbstractBlockStructurizeStairs<BlockShingle>
{
    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 3F;

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Light opacity of the block.
     */
    private static final int LIGHT_OPACITY = 255;

    /**
     * Prefix of the block.
     */
    public static final String BLOCK_PREFIX = "blockshingle_";

    public BlockShingle(final BlockState modelState, final String name)
    {
        super(modelState, Properties.create(Material.GLASS).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE).lightValue(LIGHT_OPACITY));
        setRegistryName(BLOCK_PREFIX + name);
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
