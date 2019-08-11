package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonObject;
import com.ldtteam.datagenerators.AbstractBlockStateProvider;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockTimberFrame;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.Direction;
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

        for (final Direction direction : BlockTimberFrame.FACING.getAllowedValues())
        {
            final ResourceLocation model = new ResourceLocation("structurize:block/timber_frames/" +
                    timberFrame.getTimberFrameType().getName() + "_" +
                    timberFrame.getFrameType().getName() + "_" +
                    timberFrame.getCentreType().getName() + "_timber_frame");

            final JsonObject variantObject = new JsonObject();
            setVariantModel(variantObject, model);
            if (timberFrame.getTimberFrameType().isRotatable())
            {
                setVariantX(variantObject, getXfromDirection(direction));
                setVariantY(variantObject, getYfromDirection(direction));
            }

            addVariantToVariants(blockstateJson, variantObject, "facing=" + direction.getName());
        }

        final Path blockstateFolder = this.generator.getOutputFolder().resolve(DataGeneratorConstants.BLOCKSTATE_DIR);
        final Path blockstatePath = blockstateFolder.resolve(timberFrame.getRegistryName().getPath() + ".json");

        IDataProvider.save(DataGeneratorConstants.GSON, cache, blockstateJson, blockstatePath);

    }

    private int getXfromDirection(final Direction direction)
    {
        switch (direction)
        {
            case UP:
                return 0;
            case DOWN:
                return 180;
            default:
                return 90;
        }
    }

    private int getYfromDirection(final Direction direction)
    {
        switch (direction)
        {
            default:
                return 0;
            case EAST:
                return 90;
            case SOUTH:
                return 180;
            case WEST:
                return 270;
        }
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Timber Frames BlockStates Provider";
    }
}
