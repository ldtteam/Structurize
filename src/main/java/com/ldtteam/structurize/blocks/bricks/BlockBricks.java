package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurize;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

/**
 * The class for brown, beige, and cream bricks.
 * Extends the basic `AbstractBlockStructurize` class.
 */
public class BlockBricks extends AbstractBlockStructurize<BlockBricks>
{
    public BlockBricks(final String modelName)
    {
        super(Block.Properties.from(Blocks.BRICKS));
        /**
         * Get the block properties from the vanilla brick block.
         */
        this.setRegistryName(modelName);
    }
}
