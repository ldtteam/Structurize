package com.ldtteam.structurize.blocks.bricks;

import com.ldtteam.structurize.blocks.AbstractBlockStructurizeStairs;
import com.ldtteam.structurize.blocks.ModBlocks;

/**
 * Class for brown, beige, and cream brick stairs.
 * Extends `AbstractBlockStructurizeStairs`.
 */

public class BlockBrickStairs extends AbstractBlockStructurizeStairs<BlockBrickStairs>
{

    public BlockBrickStairs(final String modelName)
    {
        super(() -> ModBlocks.blockBeigeBricks.getDefaultState(), Properties.from(ModBlocks.blockBeigeBricks));
        /**
         * Gets its block properties from the (structurize-added) beige bricks instead of (vanilla) brick stairs.
         * This is because `AbstractBlockStructurizeStairs` isn't set up to accept properties from a vanilla block and I'm too lazy to change it.
         * It should work fine.
         *
         * This is also how `BlockCactusStair.java` works.
         *
         * (Beige bricks were chosen randomly, but brown or cream bricks would also work.)
         */
        this.setRegistryName(modelName);
    }
}
