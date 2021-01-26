package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockBrickStairs extends AbstractBlockStructurizeStairs<BlockBrickStairs>
{
    public BlockBrickStairs(final String modelName)
    {
        super(Block.Properties.from(Blocks.BRICK_STAIRS));
        this.setRegistryName(modelName);
    }
}
