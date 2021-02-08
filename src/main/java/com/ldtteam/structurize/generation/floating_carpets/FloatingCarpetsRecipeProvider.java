package com.ldtteam.structurize.generation.floating_carpets;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockFloatingCarpet;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FloatingCarpetsRecipeProvider implements IDataProvider
{
    private final DataGenerator generator;

    public FloatingCarpetsRecipeProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (final BlockFloatingCarpet floatingCarpet : ModBlocks.getFloatingCarpets())
        {
            createBaseCarpetRecipe(cache, floatingCarpet);
            createDyingCarpetRecipe(cache, floatingCarpet);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Floating Carpet Recipes";
    }

    private void createBaseCarpetRecipe(final DirectoryCache cache, final BlockFloatingCarpet floatingCarpet) throws IOException
    {
        if (floatingCarpet.getRegistryName() == null || ModItems.buildTool.get().getRegistryName() == null) return;

        final ShapedRecipeJson recipeJson = new ShapedRecipeJson();
        recipeJson.setGroup("floating_carpet");
        recipeJson.setResult(new RecipeResultJson(1, floatingCarpet.getRegistryName().toString()));
        recipeJson.setPattern(new ShapedPatternJson(" B ", " C ", " S "));

        final Map<String, RecipeIngredientKeyJson> ingredients = new HashMap<>();
        ingredients.put("B", new RecipeIngredientKeyJson(new RecipeIngredientJson(ModItems.buildTool.get().getRegistryName().toString(), false)));
        ingredients.put("C", new RecipeIngredientKeyJson(new RecipeIngredientJson("minecraft:" + floatingCarpet.getColor().getTranslationKey() + "_carpet", false)));
        ingredients.put("S", new RecipeIngredientKeyJson(new RecipeIngredientJson("minecraft:string", false)));
        recipeJson.setKey(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(floatingCarpet.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }

    private void createDyingCarpetRecipe(final DirectoryCache cache, final BlockFloatingCarpet floatingCarpet) throws IOException
    {
        if (floatingCarpet.getRegistryName() == null || ModItems.buildTool.get().getRegistryName() == null) return;

        final ShapedRecipeJson recipeJson = new ShapedRecipeJson();
        recipeJson.setGroup("floating_carpet");
        recipeJson.setResult(new RecipeResultJson(8, floatingCarpet.getRegistryName().toString()));
        recipeJson.setPattern(new ShapedPatternJson("CCC", "CDC", "CCC"));

        final Map<String, RecipeIngredientKeyJson> ingredients = new HashMap<>();
        ingredients.put("C", new RecipeIngredientKeyJson(new RecipeIngredientJson("structurize:floating_carpets", true)));
        ingredients.put("D", new RecipeIngredientKeyJson(new RecipeIngredientJson("minecraft:" + floatingCarpet.getColor().getTranslationKey() + "_dye", false)));
        recipeJson.setKey(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(floatingCarpet.getRegistryName().getPath() + "_dying.json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }
}
