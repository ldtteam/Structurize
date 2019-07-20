package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.AbstractBlockStructurizeFence;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockCactusFence extends AbstractBlockStructurizeFence<BlockCactusFence>
{
    private static final String BLOCK_NAME = "blockcactusfence";

    public BlockCactusFence()
    {
        super(Block.Properties.from(Blocks.OAK_FENCE));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + BLOCK_NAME);
    }
}
