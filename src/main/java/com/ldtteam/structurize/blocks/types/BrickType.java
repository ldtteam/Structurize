package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

public enum BrickType implements IBlockCollection
{
    BROWN("brown", Items.TERRACOTTA),
    BEIGE("beige", Items.GRAVEL),
    CREAM("cream", Items.SANDSTONE);

    private static final String SUFFIX = "_brick";
    private final List<RegistryObject<Block>> blocks;
    private final String name;
    public final Item ingredient;

    BrickType(final String name, final Item ingredient)
    {
        this.name = name;
        this.ingredient = ingredient;

        blocks = create(
          ModBlocks.getRegistry(), ModItems.getRegistry(),
          ModItemGroups.CONSTRUCTION,
          BlockType.BLOCK,
          BlockType.SLAB,
          BlockType.STAIRS,
          BlockType.WALL);
    }

    @Override
    public String getName()
    {
        return this.name + SUFFIX;
    }

    @Override
    public List<RegistryObject<Block>> getRegisteredBlocks()
    {
        return blocks;
    }

    @Override
    public void provideMainRecipe(final Consumer<IFinishedRecipe> consumer, ICriterionInstance obtainment)
    {
        ShapelessRecipeBuilder.shapelessRecipe(getMainBlock(), 4)
          .addIngredient(getName().contains("stone") ? Blocks.STONE_BRICKS : Blocks.BRICKS, 2)
          .addIngredient(ingredient, 2)
          .addCriterion("has_" + getName(), obtainment)
          .build(consumer);
    }

    @Override
    public String getTextureDirectory()
    {
        return "blocks/bricks";
    }
}
