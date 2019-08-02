package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.AbstractBlockStateProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class TimberFramesBlockStateProvider extends AbstractBlockStateProvider
{
    private final DataGenerator generator;

    public TimberFramesBlockStateProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull final DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (final BlockTimberFrame timberFrame : ModBlocks.getTimberFrames())
        {
            createBlockstateFile(cache, timberFrame);
        }
    }

    private void createBlockstateFile(final DirectoryCache cache, final BlockTimberFrame timberFrame) throws IOException
    {
        if (timberFrame.getRegistryName() == null)
            return;

        final JsonObject blockstateJson = new JsonObject();

        final ResourceLocation model = new ResourceLocation("structurize:block/timber_frames/" +
                timberFrame.getTimberFrameType().getName() + "_" +
                timberFrame.getFrameType().getName() + "_" +
                timberFrame.getCentreType().getName() + "_timber_frame");

        final JsonObject variantObject = new JsonObject();
        setVariantModel(variantObject, model);

        addVariantToVariants(blockstateJson, variantObject, "");

        final Path blockstateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(timberFrame.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, blockstateJson, blockstatePath);

    }

    @NotNull
    @Override
    public String getName()
    {
        return "Timber Frames BlockStates Provider";
    }
}
