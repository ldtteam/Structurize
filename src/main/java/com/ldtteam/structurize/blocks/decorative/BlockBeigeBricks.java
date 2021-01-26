package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockBeigeBricks extends AbstractBlockStructurize<BlockBeigeBricks>
{
    private static final String BLOCK_NAME = "blockbeigebricks";

    public BlockBeigeBricks()
    {
        super(Block.Properties.from(Blocks.BRICKS));
        setRegistryName(BLOCK_NAME);
    }
}