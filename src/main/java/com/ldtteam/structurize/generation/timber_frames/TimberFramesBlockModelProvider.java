package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.models.block.BlockModelJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
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

public class TimberFramesBlockModelProvider implements IDataProvider
{
    private final DataGenerator generator;

    public TimberFramesBlockModelProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (final BlockTimberFrame timberFrame : ModBlocks.getTimberFrames())
        {
            final File modelFile = inputPath.resolve(DataGeneratorConstants.TIMBER_FRAMES_BLOCK_MODELS_DIR + timberFrame.getTimberFrameType().getName() + ".json").toFile();

            final FileReader reader = new FileReader(modelFile);
            final BlockModelJson modelJson = new BlockModelJson();
            modelJson.deserialize(new JsonParser().parse(reader));

            Map<String, String> textures = modelJson.getTextures();
            if (textures == null)
                textures = new HashMap<>();

            textures.put("frame", timberFrame.getFrameType().getTextureLocation());
            textures.put("centre", timberFrame.getCenterType().getTextureLocation());
            textures.put("particle", timberFrame.getFrameType().getTextureLocation());

            modelJson.setTextures(textures);

            final String name = timberFrame.getTimberFrameType().getName() + "_" + timberFrame.getFrameType().getName() + "_" + timberFrame.getCenterType().getName() + "_timber_frame.json";
            final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TIMBER_FRAMES_BLOCK_MODELS_DIR).resolve(name);

            IDataProvider.save(DataGeneratorConstants.GSON, cache, DataGeneratorConstants.serialize(modelJson), saveFile);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Block Model Provider";
    }
}
