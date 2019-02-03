package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockSlab;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

/**
 * Implements the half cactus slab.
 */
public class BlockCactusSlabHalf extends AbstractBlockSlab<BlockCactusSlabHalf>
{
    /**
     * Unlocalized name for the slab.
     */
    private static final String NAME = "blockcactusslab_half";

    /**
     * Constructor for the half slab.
     */
    public BlockCactusSlabHalf()
    {
        super(Material.WOOD);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + NAME);
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + NAME);
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean isDouble()
    {
        return false;
    }
}
