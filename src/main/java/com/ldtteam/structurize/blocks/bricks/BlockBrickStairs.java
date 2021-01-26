package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.blocks.ModBlocks;

public class BlockBrickStairs extends AbstractBlockStructurizeStairs<BlockBrickStairs>
{

    private static final String BLOCK_NAME = "blockbrickstairs";

    public BlockBrickStairs()
    {
        super(() -> ModBlocks.blockBeigeBricks.getDefaultState(), Properties.from(ModBlocks.blockBeigeBricks));
        setRegistryName(BLOCK_NAME);
    }
}
