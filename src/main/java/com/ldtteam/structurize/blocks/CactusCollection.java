package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.api.blocks.BlockType;
import com.ldtteam.structurize.api.blocks.IBlockCollection;
import com.ldtteam.structurize.api.generation.ModLanguageProvider;
import com.ldtteam.structurize.items.ModItemGroups;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generated collection of cactus plank blocks
 */
public class CactusCollection implements IStructurizeBlockCollection
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
    public void provideMainRecipe(final Consumer<FinishedRecipe> consumer, CriterionTriggerInstance obtainment)
    {
        ShapelessRecipeBuilder.shapeless(getMainBlock(), 4)
          .requires(Blocks.CACTUS)
          .unlockedBy("has_cactus_planks", obtainment)
          .save(consumer);
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
