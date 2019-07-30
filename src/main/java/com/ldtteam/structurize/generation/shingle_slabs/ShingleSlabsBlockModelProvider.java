package com.ldtteam.structurize.generation.shingle_slabs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.AbstractBlockModelProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.blocks.types.ShingleSlabShapeType;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class ShingleSlabsBlockModelProvider extends AbstractBlockModelProvider
{
    private final DataGenerator generator;

    public ShingleSlabsBlockModelProvider(final DataGenerator generator)
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
            for (ShingleSlabShapeType shingleSlabShape : BlockShingleSlab.SHAPE.getAllowedValues())
            {
                if (shingleSlab.getRegistryName() == null)
                    continue;

                final File modelFile = inputPath.resolve(DataGeneratorConstants.SHINGLE_SLABS_BLOCK_MODELS_DIR + "shingle_slab_" + shingleSlabShape.getName() + ".json").toFile();

                final FileReader reader = new FileReader(modelFile);
                final JsonObject modelJson = new JsonParser().parse(reader).getAsJsonObject();

                swapModelTexture(modelJson, "1", shingleSlab.getFaceType().getTexture(1));
                swapModelTexture(modelJson, "2", shingleSlab.getFaceType().getTexture(2));
                swapModelTexture(modelJson, "3", shingleSlab.getFaceType().getTexture(3));
                swapModelTexture(modelJson, "particle", shingleSlab.getFaceType().getTexture(1));

                final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.SHINGLE_SLABS_BLOCK_MODELS_DIR + shingleSlab.getRegistryName().getPath() + "_" + shingleSlabShape.getName() + ".json");

                IDataProvider.save(DataGeneratorConstants.GSON, cache, modelJson, saveFile);
            }
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Shingle Slabs Block Model Provider";
    }
}
