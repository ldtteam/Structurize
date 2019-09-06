package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockCactusPlank extends AbstractBlockStructurize<BlockCactusPlank>
{
    private static final String BLOCK_NAME = "blockcactusplank";

    public BlockCactusPlank()
    {
        super(Block.Properties.from(Blocks.OAK_DOOR));
        setRegistryName(BLOCK_NAME);
    }
}
