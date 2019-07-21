package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.blocks.ModBlocks;
import net.minecraft.block.Block;

public class BlockCactusStair extends AbstractBlockStructurizeStairs<BlockCactusStair>
{

    private static final String BLOCK_NAME = "blockcactusstair";

    public BlockCactusStair()
    {
        super(ModBlocks.blockCactusPlank.getDefaultState(), Block.Properties.from(ModBlocks.blockCactusPlank));
        setRegistryName(BLOCK_NAME);
    }
}
