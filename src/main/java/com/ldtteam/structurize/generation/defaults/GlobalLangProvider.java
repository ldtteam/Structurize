package com.ldtteam.structurize.generation.defaults;

import com.google.gson.JsonParser;
import com.ldtteam.datagenerators.lang.LangJson;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.decorative.BlockShingleSlab;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

public class GlobalLangProvider implements IDataProvider
{
    private final DataGenerator generator;
    private final LangJson backingLangJson;

    public GlobalLangProvider(DataGenerator generator, LangJson backingLangJson)
    {
        this.generator = generator;
        this.backingLangJson = backingLangJson;
    }
    
    @Override
    public void act(DirectoryCache cache) throws IOException
    {
        final Path inputPath = generator.getInputFolders().stream().findFirst().orElse(null);

        if (inputPath == null)
            return;

        final File langFile = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();
        final Reader reader = new FileReader(langFile);

        backingLangJson.deserializeOldLang(new JsonParser().parse(reader));

        IDataProvider.save(DataGeneratorConstants.GSONLang, cache, backingLangJson.serialize(), langFile.toPath());
    }

    @Override
    public String getName()
    {
        return "Global Lang Provider";
    }
}
