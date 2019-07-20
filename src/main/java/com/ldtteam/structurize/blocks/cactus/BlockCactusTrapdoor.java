package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockTrapdoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockCactusTrapdoor extends AbstractBlockTrapdoor<BlockCactusTrapdoor>
{

    private static final String BLOCK_NAME = "blockcactustrapdoor";

    public BlockCactusTrapdoor()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(3.0f).sound(SoundType.WOOD));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }
}
