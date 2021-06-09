package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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
    CREAM("cream", Items.SANDSTONE),

    BROWN_STONE("brown_stone", Items.TERRACOTTA),
    BEIGE_STONE("beige_stone", Items.GRAVEL),
    CREAM_STONE("cream_stone", Items.SANDSTONE),;

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
        ShapelessRecipeBuilder.shapeless(getMainBlock(), 4)
          .requires(getName().contains("stone") ? Blocks.STONE_BRICKS : Blocks.BRICKS, 2)
          .requires(ingredient, 2)
          .unlockedBy("has_" + getName(), obtainment)
          .save(consumer);
    }

    @Override
    public AbstractBlock.Properties getProperties()
    {
        return AbstractBlock.Properties.of(Material.STONE, MaterialColor.COLOR_RED)
                 .requiresCorrectToolForDrops()
                 .strength(2.0F, 6.0F);
    }

    @Override
    public String getTextureDirectory()
    {
        return "blocks/bricks";
    }
}
