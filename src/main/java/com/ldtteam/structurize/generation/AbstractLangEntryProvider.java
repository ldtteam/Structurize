package com.ldtteam.structurize.generation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

/**
 * Abstract class for adding a Lang entry to the EN_US lang file.
 */
public abstract class AbstractLangEntryProvider implements IDataProvider
{

    /**
     * Add a lang entry to our EN_US Lang file.
     *
     * @param cache The DirectoryCache provided in the act method of the IDataProvider class.
     * @param inputPath The provided resources directory.
     * @param reference The reference of the lang entry.
     * @param value The value of the lang entry.
     * @throws IOException Possibly thrown by the FileReader or the IDataProvider.save method.
     */
    protected void addLangEntry(final DirectoryCache cache, final Path inputPath, final String reference, final String value) throws IOException
    {
        final File langFile = inputPath.resolve(DataGeneratorConstants.EN_US_LANG).toFile();

        final Reader reader = new FileReader(langFile);
        final JsonObject langJson = new JsonParser().parse(reader).getAsJsonObject();

        langJson.addProperty(reference, value);

        IDataProvider.save(DataGeneratorConstants.GSON, cache, langJson, langFile.toPath());
    }

}
