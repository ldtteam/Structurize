package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.io.Reader;
import java.nio.file.Path;

public class TimberFramesLangEntryProvider implements IDataProvider
{
    private final DataGenerator generator;

    public TimberFramesLangEntryProvider(DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        for (BlockTimberFrame timberFrame : ModBlocks.getTimberFrames())
        {
            if (timberFrame.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + timberFrame.getRegistryName().getPath();
            final String value = timberFrame.getTimberFrameType().getLangName() + " " + timberFrame.getFrameType().getLangName() + " " + timberFrame.getCentreType().getLangName() + " Timber Frame";

            addLangEntry(cache, inputPath, reference, value);
        }
    }

    /**
     * Add a lang entry to our EN_US Lang file.
     *
     * @param cache The DirectoryCache provided in the act method of the IDataProvider class.
     * @param inputPath The provided resources directory.
     * @param reference The reference of the lang entry.
     * @param value The value of the lang entry.
     * @throws IOException Possibly thrown by the FileReader or the IDataProvider.save method.
     */
    private void addLangEntry(final DirectoryCache cache, final Path inputPath, final String reference, final String value) throws IOException
    {
        final File langFile = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();

        final Reader reader = new FileReader(langFile);
        final JsonObject langJson = new JsonParser().parse(reader).getAsJsonObject();

        langJson.addProperty(reference, value);

        IDataProvider.save(DataGeneratorConstants.GSON, cache, langJson, langFile.toPath());
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Lang Provider";
    }
}
