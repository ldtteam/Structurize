package com.ldtteam.structurize.generation.shingle_slabs;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShaplessRecipeJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShingleSlabsRecipeProvider implements IDataProvider
{
    private final DataGenerator generator;

    public ShingleSlabsRecipeProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (BlockShingleSlab shingleSlab : ModBlocks.getShingleSlabs())
        {
            if (!shingleSlab.getFaceType().isDyed())
            {
                createBasicShingleRecipe(cache, shingleSlab);
            } else
            {
                createDyableShingleRecipe(cache, shingleSlab);
            }
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingle Slab Recipes";
    }

    private void createDyableShingleRecipe(final DirectoryCache cache, final BlockShingleSlab shingleSlab) throws IOException
    {
        if (shingleSlab.getRegistryName() == null)
            return;

        final ShaplessRecipeJson recipeJson = new ShaplessRecipeJson();

        final String groupName = shingleSlab.getFaceType().getGroup() + "_shingle_slab";
        recipeJson.setGroup(groupName);
        recipeJson.setResult(new RecipeResultJson(8, shingleSlab.getRegistryName().toString()));

        final List<RecipeIngredientKeyJson> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredientKeyJson(new RecipeIngredientJson(shingleSlab.getFaceType().getRecipeIngredient(), false)));
        for(int i = 0; i < 8; i++)
        {
            ingredients.add(new RecipeIngredientKeyJson(new RecipeIngredientJson("structurize:" + groupName, true)));
        }
        recipeJson.setIngredients(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(shingleSlab.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }

    private void createBasicShingleRecipe(final DirectoryCache cache, final BlockShingleSlab shingleSlab) throws IOException
    {
        if (shingleSlab.getRegistryName() == null)
            return;

        final String groupName = shingleSlab.getFaceType().getName() + "_shingle_slab";
        final ShapedRecipeJson recipeJson = new ShapedRecipeJson();
        recipeJson.setGroup(groupName);
        recipeJson.setResult(new RecipeResultJson(8, shingleSlab.getRegistryName().toString()));
        recipeJson.setPattern(new ShapedPatternJson("   ", "III", "SSS"));

        final Map<String, RecipeIngredientKeyJson> ingredients = new HashMap<>();
        ingredients.put("I", new RecipeIngredientKeyJson(new RecipeIngredientJson(shingleSlab.getFaceType().getRecipeIngredient(), false)));
        ingredients.put("S", new RecipeIngredientKeyJson(new RecipeIngredientJson("minecraft:stick", false)));
        recipeJson.setKey(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(shingleSlab.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }
}
