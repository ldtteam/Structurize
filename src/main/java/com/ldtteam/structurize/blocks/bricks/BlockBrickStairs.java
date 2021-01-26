package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.blocks.ModBlocks;

public class BlockBrickStairs extends AbstractBlockStructurizeStairs<BlockBrickStairs>
{

    public BlockBrickStairs(final String modelName)
    {
        super(() -> ModBlocks.blockBeigeBricks.getDefaultState(), Properties.from(ModBlocks.blockBeigeBricks));
        this.setRegistryName(modelName);
    }
}
