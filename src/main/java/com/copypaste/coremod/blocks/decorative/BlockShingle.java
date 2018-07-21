package com.copypaste.coremod.blocks.decorative;

import com.copypaste.api.util.constant.Constants;
import com.copypaste.coremod.blocks.AbstractBlockCopyPasteStairs;
import com.copypaste.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.state.IBlockState;

import java.util.Locale;

public class BlockShingle extends AbstractBlockCopyPasteStairs<BlockShingle>
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

    public BlockShingle(final IBlockState modelState, final String name)
    {
        super(modelState);
        init(name);
    }

    private void init(final String name)
    {
        setRegistryName(name);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.US), name));
        setCreativeTab(ModCreativeTabs.COPY_PASTE);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
        this.useNeighborBrightness = true;
        this.setLightOpacity(LIGHT_OPACITY);
    }
}
