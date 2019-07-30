package com.ldtteam.structurize.generation.shingles;

import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.AbstractRecipeProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class ShinglesRecipeProvider extends AbstractRecipeProvider
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
            }
            else
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

        final String name = shingle.getRegistryName().getPath();
        final String groupName = shingle.getFaceType().getGroup() + "_" + shingle.getWoodType().getName() + "_shingle";

        final ShaplessIngredient dyeIngredient = new ShaplessIngredient("item", shingle.getFaceType().getRecipeIngredient());
        final ShaplessIngredient shingleIngredient = new ShaplessIngredient("tag", new ResourceLocation("structurize:" + groupName));

        final JsonObject recipe = createShaplessRecipe(shingle.getRegistryName(), 1, dyeIngredient, shingleIngredient);

        setRecipeGroup(recipe, groupName);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, recipe, recipePath);
    }

    private void createBasicShingleRecipe(final DirectoryCache cache, final BlockShingle shingle) throws IOException
    {
        if (shingle.getRegistryName() == null)
            return;

        final String name = shingle.getRegistryName().getPath();
        final String groupName = shingle.getFaceType().getName() + "_shingle";

        final ShapedIngredient faceIngredient = new ShapedIngredient("item", "I", shingle.getFaceType().getRecipeIngredient());
        final ShapedIngredient stickIngredient = new ShapedIngredient("item", "S", new ResourceLocation("minecraft:stick"));
        final ShapedIngredient woodIngredient = new ShapedIngredient("item", "P", shingle.getWoodType().getRecipeIngredient());

        final JsonObject recipe = createShapedRecipe(shingle.getRegistryName(), 8, "I  ", "SI ", "PSI", faceIngredient, stickIngredient, woodIngredient);

        setRecipeGroup(recipe, groupName);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, recipe, recipePath);
    }
}
