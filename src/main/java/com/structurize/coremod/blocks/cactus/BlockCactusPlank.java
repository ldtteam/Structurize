package com.structurize.coremod.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.blocks.AbstractBlockStructurize;
import com.structurize.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusPlank extends AbstractBlockStructurize<BlockCactusPlank>
{


    public BlockCactusPlank()
    {
        super(Material.WOOD, MapColor.GREEN);
        setRegistryName("blockcactusplank");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusplank");
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);

    }
}
