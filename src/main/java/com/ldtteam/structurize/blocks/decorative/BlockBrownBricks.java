package com.ldtteam.structurize.blocks.decorative;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockBrownBricks extends AbstractBlockStructurize<BlockBrownBricks>
{
    private static final String BLOCK_NAME = "blockbrownbricks";

    public BlockBrownBricks()
    {
        super(Block.Properties.from(Blocks.BRICKS));
        setRegistryName(BLOCK_NAME);
    }
}