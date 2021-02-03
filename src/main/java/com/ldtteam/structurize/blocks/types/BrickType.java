package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;

import static com.ldtteam.structurize.blocks.ModBlocks.getList;

public enum BrickType implements IBlockCollection
{
    BROWN("brown", Items.TERRACOTTA),
    BEIGE("beige", Items.GRAVEL),
    CREAM("cream", Items.SANDSTONE);

    private static final String                 SUFFIX = "_bricks";
    private final List<RegistryObject<Block>> blocks;
    private final        String                 name;
    public final         Item            ingredient;

    BrickType(final String name, final Item ingredient)
    {
        this.name = name;
        this.ingredient = ingredient;

        blocks = create(
          ModBlocks.BLOCKS, ModItems.ITEMS,
          IBlockCollection.BlockType.BLOCK,
          IBlockCollection.BlockType.SLAB,
          IBlockCollection.BlockType.STAIRS,
          IBlockCollection.BlockType.WALL);
    }

    @Override
    public String getName()
    {
        return this.name + SUFFIX;
    }

    @Override
    public List<Block> getBlocks()
    {
        return getList(blocks);
    }
}
