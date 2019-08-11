package com.ldtteam.structurize.generation.shingle_slabs;

import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.AbstractRecipeProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class ShingleSlabsRecipeProvider extends AbstractRecipeProvider
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
            }
            else
            {
                createDyableShingleRecipe(cache, shingleSlab);
            }
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingles Recipes";
    }

    private void createDyableShingleRecipe(final DirectoryCache cache, final BlockShingleSlab shingleSlab) throws IOException
    {
        if (shingleSlab.getRegistryName() == null)
            return;

        final String name = shingleSlab.getRegistryName().getPath();
        final String groupName = shingleSlab.getFaceType().getGroup() + "_shingle_slab";

        final ShaplessIngredient dyeIngredient = new ShaplessIngredient("item", shingleSlab.getFaceType().getRecipeIngredient());
        final ShaplessIngredient shingleIngredient = new ShaplessIngredient("tag", new ResourceLocation("structurize:" + groupName));

        final JsonObject recipe = createShaplessRecipe(shingleSlab.getRegistryName(), 1, dyeIngredient, shingleIngredient);

        setRecipeGroup(recipe, groupName);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, recipe, recipePath);
    }

    private void createBasicShingleRecipe(final DirectoryCache cache, final BlockShingleSlab shingleSlab) throws IOException
    {
        if (shingleSlab.getRegistryName() == null)
            return;

        final String name = shingleSlab.getRegistryName().getPath();
        final String groupName = shingleSlab.getFaceType().getName() + "_shingle_slab";

        final ShapedIngredient faceIngredient = new ShapedIngredient("item", "I", shingleSlab.getFaceType().getRecipeIngredient());
        final ShapedIngredient stickIngredient = new ShapedIngredient("item", "S", new ResourceLocation("minecraft:stick"));

        final JsonObject recipe = createShapedRecipe(shingleSlab.getRegistryName(), 8, "   ", "III", "SSS", faceIngredient, stickIngredient);

        setRecipeGroup(recipe, groupName);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, recipe, recipePath);
    }
}
