package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.generation.defaults.DefaultProviderTemplates;
import com.ldtteam.structurize.generation.defaults.ProviderSet;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.ldtteam.structurize.generation.defaults.DefaultProviderTemplates.*;

public enum BrickType implements IStringSerializable, BlockSet<BrickType>
{
    BROWN("brown", Items.TERRACOTTA),
    BEIGE("beige", Items.GRAVEL),
    CREAM("cream", Items.SANDSTONE);

    private static final String SUFFIX = "_bricks";
    private final String name;
    private final Item   ingredient;
    private       Block  normal;

    BrickType(final String name, final Item ingredient)
    {
        this.name = name;
        this.ingredient = ingredient;
    }

    @Override
    public String getString()
    {
        return this.name + SUFFIX;
    }

    @NotNull
    public String getName()
    {
        return this.name;
    }

    @Nullable
    @Override
    public BrickType fromSearch(final String search)
    {
        return search.equals(getString()) ? this : null;
    }

    public Item getIngredient()
    {
        return ingredient;
    }

    public Block setNormalBlock(Block normal)
    {
        this.normal = normal;
        return normal;
    }

    public Block getNormalBlock()
    {
        return this.normal;
    }

    public static IDataProvider getProvider(DataGenerator gen)
    {
        return new ProviderSet(gen, ModBlocks.getBricks())
          .variants(blockstateFromParentSet("minecraft:block/bricks"))
          .item(itemModelFromParentSet("minecraft:block/bricks"))
          .shapeless(
            block -> CutType.fromSuffix(block) == CutType.NORMAL,
            block -> new ShapelessRecipeBuilder(block, 9)
                       .setGroup("bricks")
                       .addIngredient(Items.BRICKS, 8)
                       .addIngredient(BlockSet.find(block, BrickType.values()).getIngredient()))
          .shaped(
            block -> true, // for the blocks that fail the previous test (i.e. are not the normal form)
            block -> populateStandardSetRecipes(BlockSet.find(block, BrickType.values()).getNormalBlock().asItem(),null).apply(block));
    }
}
