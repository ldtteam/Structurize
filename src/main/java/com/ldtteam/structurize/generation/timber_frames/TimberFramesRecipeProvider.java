package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.AbstractRecipeProvider;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.blocks.types.TimberFrameType;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import com.ldtteam.structurize.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class TimberFramesRecipeProvider extends AbstractRecipeProvider
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

        for ( int i = 0; i < ModBlocks.getTimberFrames().size(); i++ )
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

        String name = timberFrame.getRegistryName().getPath();
        if (timberFrame.getTimberFrameType().equals(TimberFrameType.PLAIN))
            name = timberFrame.getRegistryName().getPath() + "_cycle";

        final String groupName = timberFrame.getFrameType().getName() + "_" + timberFrame.getCentreType().getName() + "_timber_frame";

        final TimberFrameType previous = timberFrame.getTimberFrameType().getPrevious();

        final ShaplessIngredient timberFrameIngredient = new ShaplessIngredient("item",
                new ResourceLocation(Constants.MOD_ID,
                        BlockTimberFrame.getName(previous,
                        timberFrame.getFrameType(),
                        timberFrame.getCentreType())));

        final JsonObject recipe = createShaplessRecipe(timberFrame.getRegistryName(), 1, timberFrameIngredient);

        setRecipeGroup(recipe, groupName);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, recipe, recipePath);
    }

    private void createPlainTimberFrameRecipe(final DirectoryCache cache, final BlockTimberFrame timberFrame) throws IOException
    {
        if (timberFrame.getRegistryName() == null)
            return;

        final String name = timberFrame.getRegistryName().getPath();
        final String groupName = timberFrame.getFrameType().getName() + "_" + timberFrame.getCentreType().getName() + "_timber_frame";

        final ShapedIngredient frameIngredient = new ShapedIngredient("item", "F", timberFrame.getFrameType().getRecipeIngredient());
        final ShapedIngredient centreIngredient = new ShapedIngredient("item", "C", timberFrame.getCentreType().getRecipeIngredient());
        final ShapedIngredient scepterIngredient = new ShapedIngredient("item", "S", ModItems.buildTool.getRegistryName());

        final JsonObject recipe = createShapedRecipe(timberFrame.getRegistryName(), 4, " F ", " C ", " S ", frameIngredient, centreIngredient, scepterIngredient);

        setRecipeGroup(recipe, groupName);

        final Path recipePath = this.generator.getOutputFolder().resolve(DataGeneratorConstants.RECIPES_DIR).resolve(name + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, recipe, recipePath);
    }
}
