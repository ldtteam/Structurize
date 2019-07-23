package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
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
     * Prefix of the block.
     */
    public static final String BLOCK_PREFIX = "blockshingle_";

    public BlockShingle(final BlockState modelState, final String name)
    {
        super(modelState, Properties.create(Material.GLASS).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE));
        setRegistryName(BLOCK_PREFIX + name);
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
