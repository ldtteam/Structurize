package com.ldtteam.structurize.generation.shingles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.AbstractBlockModelProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingle;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.state.properties.StairsShape;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class ShinglesBlockModelProvider extends AbstractBlockModelProvider
{
    private final DataGenerator generator;

    public ShinglesBlockModelProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (final BlockShingle shingle : ModBlocks.getShingles())
        {
            for (StairsShape shapeValue : StairsBlock.SHAPE.getAllowedValues())
            {
                final String shapeType = BlockShingle.getTypeFromShape(shapeValue);

                final File modelFile = inputPath.resolve(DataGeneratorConstants.SHINGLES_BLOCK_MODELS_DIR + "shingle_" + shapeType + ".json").toFile();

                final FileReader reader = new FileReader(modelFile);
                final JsonObject modelJson = new JsonParser().parse(reader).getAsJsonObject();

                swapModelTexture(modelJson, "1", shingle.getFaceType().getTexture(1));
                swapModelTexture(modelJson, "2", shingle.getFaceType().getTexture(2));
                swapModelTexture(modelJson, "3", shingle.getFaceType().getTexture(3));
                swapModelTexture(modelJson, "particle", shingle.getFaceType().getTexture(1));
                swapModelTexture(modelJson, "plank", shingle.getWoodType().getTextureLocation());

                final String name = shingle.getFaceType().getName() + "_shingle" + ".json";
                final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.SHINGLES_BLOCK_MODELS_DIR + shapeType + "/" + shingle.getWoodType().getName()).resolve(name);

                IDataProvider.save(DataGeneratorConstants.GSON, cache, modelJson, saveFile);
            }
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Shingles Block Model Provider";
    }
}
