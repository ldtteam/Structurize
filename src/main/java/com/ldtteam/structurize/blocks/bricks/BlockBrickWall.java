package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeWall;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

/**
 * The class for brown, beige, and cream brick walls.
 * Extends `AbstractBlockStructurizeWall`.
 */

public class BlockBrickWall extends AbstractBlockStructurizeWall<BlockBrickWall>
{
    public BlockBrickWall(final String modelName)
    {
        super(Block.Properties.from(Blocks.BRICK_WALL));
        /**
         * Get the block properties from the vanilla brick wall.
         */
        this.setRegistryName(modelName);
    }
}
