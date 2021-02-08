package com.ldtteam.structurize.generation.timber_frames;

import com.ldtteam.datagenerators.recipes.RecipeIngredientJson;
import com.ldtteam.datagenerators.recipes.RecipeIngredientKeyJson;
import com.ldtteam.datagenerators.recipes.RecipeResultJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedPatternJson;
import com.ldtteam.datagenerators.recipes.shaped.ShapedRecipeJson;
import com.ldtteam.datagenerators.recipes.shapeless.ShapelessRecipeJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import com.ldtteam.structurize.items.ModItems;
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

public class TimberFramesRecipeProvider implements IDataProvider
{
    private final DataGenerator generator;

    public TimberFramesRecipeProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (int i = 0; i < ModBlocks.getTimberFrames().size(); i++)
        {
            final BlockTimberFrame timberFrame = ModBlocks.getTimberFrames().get(i);

            if (timberFrame.getTimberFrameType().equals(TimberFrameType.PLAIN))
            {
                createPlainTimberFrameRecipe(cache, timberFrame);
            }
            createDyableShingleRecipe(cache, timberFrame);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frame Recipes";
    }

    private void createDyableShingleRecipe(final DirectoryCache cache, final BlockTimberFrame timberFrame) throws IOException
    {
        if (timberFrame.getRegistryName() == null)
            return;

        final ShapelessRecipeJson recipeJson = new ShapelessRecipeJson();

        final String groupName = timberFrame.getFrameType().getName() + "_" + timberFrame.getCentreType().getName() + "_timber_frame";

        recipeJson.setGroup(groupName);
        recipeJson.setResult(new RecipeResultJson(1, timberFrame.getRegistryName().toString()));

        final List<RecipeIngredientKeyJson> ingredients = new ArrayList<>();

        final TimberFrameType previous = timberFrame.getTimberFrameType().getPrevious();
        final String recipeIngredient = "structurize:" + BlockTimberFrame.getName(previous, timberFrame.getFrameType(), timberFrame.getCentreType());

        ingredients.add(new RecipeIngredientKeyJson(new RecipeIngredientJson(recipeIngredient, false)));

        recipeJson.setIngredients(ingredients);

        String name = timberFrame.getRegistryName().getPath();
        if (timberFrame.getTimberFrameType().equals(TimberFrameType.PLAIN))
            name = timberFrame.getRegistryName().getPath() + "_cycle";

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }

    private void createPlainTimberFrameRecipe(final DirectoryCache cache, final BlockTimberFrame timberFrame) throws IOException
    {
        if (timberFrame.getRegistryName() == null || ModItems.buildTool.get().getRegistryName() == null)
            return;

        final String name = timberFrame.getRegistryName().getPath();
        final String groupName = timberFrame.getFrameType().getName() + "_" + timberFrame.getCentreType().getName() + "_timber_frame";

        final ShapedRecipeJson recipeJson = new ShapedRecipeJson();
        recipeJson.setGroup(groupName);
        recipeJson.setPattern(new ShapedPatternJson(" F ", " C ", " S "));
        recipeJson.setResult(new RecipeResultJson(4, timberFrame.getRegistryName().toString()));

        final Map<String, RecipeIngredientKeyJson> ingredients = new HashMap<>();
        final RecipeIngredientJson ingredientF = new RecipeIngredientJson(timberFrame.getFrameType().getRecipeIngredient(), false);
        ingredients.put("F", new RecipeIngredientKeyJson(ingredientF));
        final RecipeIngredientJson ingredientC = new RecipeIngredientJson(timberFrame.getCentreType().getRecipeIngredient(), false);
        ingredients.put("C", new RecipeIngredientKeyJson(ingredientC));
        final RecipeIngredientJson ingredientS = new RecipeIngredientJson(ModItems.buildTool.get().getRegistryName().toString(), false);
        ingredients.put("S", new RecipeIngredientKeyJson(ingredientS));

        recipeJson.setKey(ingredients);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(recipeJson), recipePath);
    }
}
