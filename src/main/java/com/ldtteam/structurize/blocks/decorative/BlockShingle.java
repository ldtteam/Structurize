package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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
    public static final String BLOCK_PREFIX = "blockshingle";

    public BlockShingle(final BlockState modelState, final String name)
    {
        super(modelState);
        init(name);
    }

    private void init(final String name)
    {
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + name);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        this.useNeighborBrightness = true;
        this.setLightOpacity(LIGHT_OPACITY);
    }

    @NotNull
    @Override
    public BlockFaceShape getBlockFaceShape(@NotNull final IBlockAccess worldIn, @NotNull final BlockState state, @NotNull final BlockPos pos, final EnumFacing face)
    {
        if (face == EnumFacing.UP)
        {
            return BlockFaceShape.CENTER_BIG;
        }
        return super.getBlockFaceShape(worldIn, state, pos, face);
    }
}
