package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.api.generation.ModLanguageProvider;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Generated collection of cactus plank blocks
 */
public class CactusCollection implements IBlockCollection
{
    protected final List<RegistryObject<Block>> blocks;

    public CactusCollection()
    {
        blocks = create(
          ModBlocks.getRegistry(), ModItems.getRegistry(),
          ModItemGroups.CONSTRUCTION,
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
    public List<RegistryObject<Block>> getRegisteredBlocks()
    {
        return blocks;
    }

    @Override
    public void provideMainRecipe(final Consumer<IFinishedRecipe> consumer, ICriterionInstance obtainment)
    {
        ShapelessRecipeBuilder.shapelessRecipe(getMainBlock(), 4)
          .addIngredient(Blocks.CACTUS)
          .addCriterion("has_cactus_planks", obtainment)
          .build(consumer);
    }

    // TODO 1.17 use the default translation, as it will then work with the normal registry keys
    @Override
    public void generateTranslations(final ModLanguageProvider lang)
    {
        lang.add(getRegisteredBlocks().get(0).get(), "Cactus Planks");
        lang.add(getRegisteredBlocks().get(1).get(), "Cactus Slab");
        lang.add(getRegisteredBlocks().get(2).get(), "Cactus Stairs");
        lang.add(getRegisteredBlocks().get(3).get(), "Cactus Fence");
        lang.add(getRegisteredBlocks().get(4).get(), "Cactus Fence Gate");
        lang.add(getRegisteredBlocks().get(5).get(), "Cactus Door");
        lang.add(getRegisteredBlocks().get(6).get(), "Cactus Trapdoor");
    }

    @Override
    public String getTextureDirectory()
    {
        return "blocks/cactus";
    }
}
