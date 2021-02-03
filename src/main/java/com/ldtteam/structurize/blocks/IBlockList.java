package com.ldtteam.structurize.blocks;

import net.minecraft.block.Block;
import net.minecraft.util.IStringSerializable;

public interface IBlockList<B extends Block> extends IStringSerializable
{
    B construct();

    String getName();
}
