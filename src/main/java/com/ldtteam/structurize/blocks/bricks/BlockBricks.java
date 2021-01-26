package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockBricks extends AbstractBlockStructurize<BlockBricks>
{
    private static final String BLOCK_NAME = "blockbricks";

    public BlockBricks()
    {
        super(Block.Properties.from(Blocks.BRICKS));
        setRegistryName(BLOCK_NAME);
    }
}