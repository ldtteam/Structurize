package com.ldtteam.structurize.blocks;

import com.ldtteam.structurize.blocks.types.IBlockCollection;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.function.Consumer;

public class CactusCollection implements IBlockCollection
{
    protected final List<RegistryObject<Block>> blocks;

    public CactusCollection()
    {
        blocks = create(
          ModBlocks.getRegistry(), ModItems.getRegistry(),
          ItemGroup.BUILDING_BLOCKS,
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
    public List<RegistryObject<Block>> getBlocks()
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
}
