package com.ldtteam.structurize.generation.timber_frames;

import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.lang.LangJson;
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

        final File langFile = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();
        final Reader reader = new FileReader(langFile);

        final LangJson langJson = new LangJson();
        langJson.deserialize(new JsonParser().parse(reader));

        for (BlockTimberFrame timberFrame : ModBlocks.getTimberFrames())
        {
            if (timberFrame.getRegistryName() == null)
                continue;

            final String reference = "block.structurize." + timberFrame.getRegistryName().getPath();
            final String value = timberFrame.getTimberFrameType().getLangName() + " " + timberFrame.getFrameType().getLangName() + " " + timberFrame.getCentreType().getLangName() + " Timber Frame";

            langJson.getLang().put(reference, value);
        }

        IDataProvider.save(DataGeneratorConstants.GSON, cache, langJson.serialize(), langFile.toPath());

    }

    @Override
    @NotNull
    public String getName()
    {
        return "Timber Frames Lang Provider";
    }
}
