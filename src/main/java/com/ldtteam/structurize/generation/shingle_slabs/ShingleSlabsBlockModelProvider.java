package com.ldtteam.structurize.generation.shingle_slabs;

import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.models.block.BlockModelJson;
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
import java.util.HashMap;
import java.util.Map;

public class ShingleSlabsBlockModelProvider implements IDataProvider
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
                final BlockModelJson modelJson = new BlockModelJson();
                modelJson.deserialize(new JsonParser().parse(reader));
                modelJson.setAmbientOcclusion(false);

                Map<String, String> textures = modelJson.getTextures();
                if (textures == null)
                    textures = new HashMap<>();

                textures.put("1", shingleSlab.getFaceType().getTexture(1));
                textures.put("2", shingleSlab.getFaceType().getTexture(2));
                textures.put("3", shingleSlab.getFaceType().getTexture(3));
                textures.put("particle", shingleSlab.getFaceType().getTexture(1));

                modelJson.setTextures(textures);

                final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.SHINGLE_SLABS_BLOCK_MODELS_DIR + shingleSlab.getRegistryName().getPath() + "_" + shingleSlabShape.getName() + ".json");

                IDataProvider.save(DataGeneratorConstants.GSON, cache, modelJson.serialize(), saveFile);
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
