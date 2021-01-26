package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockBricks extends AbstractBlockStructurize<BlockBricks>
{
    public BlockBricks(final String modelName)
    {
        super(Block.Properties.from(Blocks.BRICKS));
        this.setRegistryName(modelName);
    }
}