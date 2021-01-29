package com.ldtteam.structurize.generation.defaults;

import com.ldtteam.datagenerators.blockstate.BlockstateModelJson;
import com.ldtteam.datagenerators.blockstate.BlockstateVariantJson;
import com.ldtteam.datagenerators.models.item.ItemModelJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShapelessRecipeJson;
import com.ldtteam.structurize.blocks.types.BlockSet;
import net.minecraft.block.*;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class DefaultProviderTemplates
{
    private DefaultProviderTemplates() { /* prevent construction */ }

    public static String getBlockSetPath(String path, Block block)
    {
        if (block instanceof SlabBlock) return path + "_slab";
        if (block instanceof StairsBlock) return path + "_stairs";
        if (block instanceof WallBlock) return path + "_wall";
        if (block instanceof FenceBlock) return path + "_fence";
        if (block instanceof FenceGateBlock) return path + "_fence_gate";
        return path;
    }

    public static String toTranslation(String path)
    {
        List<String> name = new ArrayList<>();
        for (String word : path.split("[_. /]"))
        {
            name.add(word.substring(0,1).toUpperCase(Locale.US) + word.substring(1));
        }
        return String.join(" ", name);
    }

    public static String toTranslation(Block block)
    {
        return toTranslation(block.getRegistryName().getPath());
    }

    // == BlockStates == //

    public static BiConsumer<Block, Map<String, BlockstateVariantJson>> SIMPLE(String path)
    {
        return (block, json) -> json.put(
          "",
          new BlockstateVariantJson(
            new BlockstateModelJson(
              path.equals("")
                ? block.getRegistryName().getNamespace() + ":block/" + block.getRegistryName().getPath()
                : path)));
    }

    public static BiConsumer<Block, Map<String, BlockstateVariantJson>> blockstateFromParentSet(String path)
    {
        return (block, json) -> json.put("", new BlockstateVariantJson(new BlockstateModelJson(getBlockSetPath(path, block))));
    }

    // == ItemModels == //

    public static BiConsumer<Block, ItemModelJson> itemModelFromParentSet(String parent)
    {
        return (block, json) -> json.setParent(getBlockSetPath(parent, block));
    }

    // == Recipes == //

    public static Function<Block, Tuple<Integer, String>> INGREDIENT(final int quantity, final String path)
    {
        return block -> new Tuple<>(quantity, path);
    }

    public static Function<Block, ShapedRecipeBuilder> populateStandardSetRecipes(final Item mainMaterial, final Item stickMaterial)
    {
        return block -> {
            BlockSet.CutType cut = BlockSet.CutType.fromSuffix(block.getRegistryName().getPath());
            return cut != null? cut.formRecipe(block, mainMaterial, stickMaterial) : null;
        };
    }
}
