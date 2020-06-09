package com.ldtteam.structurize.generation.shingles;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShaplessRecipeJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
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

public class ShinglesRecipeProvider implements IDataProvider
{
    private final DataGenerator generator;

    public ShinglesRecipeProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (BlockShingle shingle : ModBlocks.getShingles())
        {
            if (!shingle.getFaceType().isDyed())
            {
                createBasicShingleRecipe(cache, shingle);
            } else
            {
                createDyableShingleRecipe(cache, shingle);
            }
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingles Recipes";
    }

    private void createDyableShingleRecipe(final DirectoryCache cache, final BlockShingle shingle) throws IOException
    {
        if (shingle.getRegistryName() == null)
            return;

        final ShaplessRecipeJson recipeJson = new ShaplessRecipeJson();

        final String groupName = shingle.getFaceType().getGroup() + "_" + shingle.getWoodType().getName() + "_shingle";
        recipeJson.setGroup(groupName);
        recipeJson.setResult(new RecipeResultJson(8, shingle.getRegistryName().toString()));

        final List<RecipeIngredientKeyJson> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredientKeyJson(new RecipeIngredientJson(shingle.getFaceType().getRecipeIngredient(), false)));
        for(int i = 0; i < 8; i++)
        {
            ingredients.add(new RecipeIngredientKeyJson(new RecipeIngredientJson("structurize:shingles/" + shingle.getFaceType().getGroup() + "/" + shingle.getWoodType().getName(), true)));
        }
        recipeJson.setIngredients(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(shingle.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }

    private void createBasicShingleRecipe(final DirectoryCache cache, final BlockShingle shingle) throws IOException
    {
        if (shingle.getRegistryName() == null)
            return;

        final String groupName = shingle.getFaceType().getName() + "_shingle";
        final ShapedRecipeJson recipeJson = new ShapedRecipeJson();
        recipeJson.setGroup(groupName);
        recipeJson.setResult(new RecipeResultJson(8, shingle.getRegistryName().toString()));
        recipeJson.setPattern(new ShapedPatternJson("I  ", "SI ", "PSI"));

        final Map<String, RecipeIngredientKeyJson> ingredients = new HashMap<>();
        ingredients.put("I", new RecipeIngredientKeyJson(new RecipeIngredientJson(shingle.getFaceType().getRecipeIngredient(), false)));
        ingredients.put("S", new RecipeIngredientKeyJson(new RecipeIngredientJson("minecraft:stick", false)));
        ingredients.put("P", new RecipeIngredientKeyJson(new RecipeIngredientJson(shingle.getWoodType().getRecipeIngredient(), false)));
        recipeJson.setKey(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(shingle.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }
}
