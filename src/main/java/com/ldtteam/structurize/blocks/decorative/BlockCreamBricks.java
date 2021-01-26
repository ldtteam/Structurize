package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockCreamBricks extends AbstractBlockStructurize<BlockCreamBricks>
{
    private static final String BLOCK_NAME = "blockcreambricks";

    public BlockCreamBricks()
    {
        super(Block.Properties.from(Blocks.BRICKS));
        setRegistryName(BLOCK_NAME);
    }
}