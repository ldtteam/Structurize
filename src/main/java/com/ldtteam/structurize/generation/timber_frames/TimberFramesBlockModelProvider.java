package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.AbstractBlockModelProvider;
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

public class TimberFramesBlockModelProvider extends AbstractBlockModelProvider
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
            final JsonObject modelJson = new JsonParser().parse(reader).getAsJsonObject();

            swapModelTexture(modelJson, "frame", timberFrame.getFrameType().getTextureLocation());
            swapModelTexture(modelJson, "centre", timberFrame.getCentreType().getTextureLocation());
            swapModelTexture(modelJson, "particle", timberFrame.getFrameType().getTextureLocation());

            final String name = timberFrame.getTimberFrameType().getName() + "_" + timberFrame.getFrameType().getName() + "_" + timberFrame.getCentreType().getName() + "_timber_frame.json";
            final Path saveFile = this.generator.getOutputFolder().resolve(DataGeneratorConstants.TIMBER_FRAMES_BLOCK_MODELS_DIR).resolve(name);

            IDataProvider.save(DataGeneratorConstants.GSON, cache, modelJson, saveFile);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Block Model Provider";
    }
}
