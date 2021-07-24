package com.ldtteam.structurize.blocks.types;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.blocks.IStructurizeBlockCollection;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

public enum BrickType implements IStructurizeBlockCollection
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
    public void provideMainRecipe(final Consumer<FinishedRecipe> consumer, CriterionTriggerInstance obtainment)
    {
        ShapelessRecipeBuilder.shapeless(getMainBlock(), 4)
          .requires(getName().contains("stone") ? Blocks.STONE_BRICKS : Blocks.BRICKS, 2)
          .requires(ingredient, 2)
          .unlockedBy("has_" + getName(), obtainment)
          .save(consumer);
    }

    @Override
    public BlockBehaviour.Properties getProperties()
    {
        return BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED)
                 .requiresCorrectToolForDrops()
                 .strength(2.0F, 6.0F);
    }

    @Override
    public String getTextureDirectory()
    {
        return "blocks/bricks";
    }
}
