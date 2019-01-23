package com.structurize.coremod.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.blocks.AbstractBlockTrapdoor;
import com.structurize.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusTrapdoor extends AbstractBlockTrapdoor<BlockCactusTrapdoor>
{

    public BlockCactusTrapdoor()
    {
        super(Material.WOOD);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactustrapdoor");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactustrapdoor");
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
    }
}
