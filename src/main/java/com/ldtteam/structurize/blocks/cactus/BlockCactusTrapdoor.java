package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.blocks.AbstractBlockTrapdoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class BlockCactusTrapdoor extends AbstractBlockTrapdoor<BlockCactusTrapdoor>
{
    private static final String BLOCK_NAME = "blockcactustrapdoor";

    public BlockCactusTrapdoor()
    {
        super(Properties.create(Material.WOOD, MaterialColor.CYAN).hardnessAndResistance(3.0f).sound(SoundType.WOOD));
        setRegistryName(BLOCK_NAME);
    }
}
