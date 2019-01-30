package com.ldtteam.structurize.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import java.util.Locale;

public class BlockCactusStair extends AbstractBlockStructurizeStairs<BlockCactusStair>
{

    public BlockCactusStair(final IBlockState modelState)
    {
        super(modelState);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactusstair");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusstair");
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);
    }
}
