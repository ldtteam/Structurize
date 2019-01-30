package com.ldtteam.structurize.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeFence;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusFence extends AbstractBlockStructurizeFence<BlockCactusFence>
{
    public BlockCactusFence()
    {
        super(Material.WOOD, MapColor.GREEN);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactusfence");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusfence");
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);
    }
}
