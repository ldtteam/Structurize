package com.structurize.coremod.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.blocks.AbstractBlockStructurizeStairs;
import com.structurize.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import java.util.Locale;

public class BlockCactusStair extends AbstractBlockStructurizeStairs<BlockCactusStair>
{

    public BlockCactusStair(final IBlockState modelState)
    {
        super(modelState);
        setRegistryName("blockcactusstair");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusstair");
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);
    }
}
