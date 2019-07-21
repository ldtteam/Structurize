package com.ldtteam.structurize.blocks.cactus;

import com.ldtteam.structurize.blocks.AbstractBlockDoor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BlockCactusDoor extends AbstractBlockDoor<BlockCactusDoor>
{
    private static final String BLOCK_NAME = "blockcactusdoor";

    public BlockCactusDoor()
    {
        super(Block.Properties.from(Blocks.OAK_DOOR));
        setRegistryName(BLOCK_NAME);
    }
}
