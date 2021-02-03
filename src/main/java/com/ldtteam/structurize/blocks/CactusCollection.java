package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;

import static com.ldtteam.structurize.blocks.ModBlocks.BLOCKS;
import static com.ldtteam.structurize.blocks.ModBlocks.getList;
import static com.ldtteam.structurize.items.ModItems.ITEMS;

public class CactusCollection implements IBlockCollection
{
    protected final List<RegistryObject<Block>> blocks;

    public CactusCollection()
    {
        blocks = create(
          BLOCKS, ITEMS,
          BlockType.PLANKS,
          BlockType.SLAB,
          BlockType.STAIRS,
          BlockType.FENCE,
          BlockType.FENCE_GATE,
          BlockType.DOOR,
          BlockType.TRAPDOOR);
    }

    @Override
    public String getName()
    {
        return "cactus";
    }

    @Override
    public List<Block> getBlocks()
    {
        return getList(blocks);
    }
}
