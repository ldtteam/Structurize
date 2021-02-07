package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;

import static com.ldtteam.structurize.blocks.ModBlocks.getList;

public class CactusCollection implements IBlockCollection
{
    protected final List<RegistryObject<Block>> blocks;

    public CactusCollection()
    {
        blocks = create(
          ModBlocks.getRegistry(), ModItems.getRegistry(),
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
        // TODO 1.17 restore just "cactus"
        return "blockcactus";
    }

    @Override
    public String getPluralName()
    {
        return getName();
    }

    @Override
    public List<Block> getBlocks()
    {
        return getList(blocks);
    }
}
