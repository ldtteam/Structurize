package com.ldtteam.structurize.management.schemaserver.fm;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.schemaserver.utils.BetterGSON;
import com.ldtteam.structurize.management.schemaserver.utils.BetterGSON.Additions;

public class StylesHeadFile
{
    public static final String JSON_NAME = "styles.json";

    private Map<String, StyleEntry> files;
    private Path stylesFolder;

    private StylesHeadFile()
    {
    }

    public Path getStylesFolder()
    {
        return stylesFolder;
    }

    private void checkFieldsNotNull(final Path userRoot)
    {
        if (files == null)
        {
            files = new HashMap<>();
        }
        if (stylesFolder == null)
        {
            stylesFolder = userRoot.resolve("styles");
        }
    }

    public static StylesHeadFile read(final Path userRoot) throws IOException
    {
        final StylesHeadFile result;
        final Path jsonPath = userRoot.resolve(JSON_NAME);

        if (Files.exists(jsonPath))
        {
            final JsonReader jsonReader = new JsonReader(Files.newBufferedReader(jsonPath));
            result = BetterGSON.withCreate(Additions.PATH).fromJson(jsonReader, StylesHeadFile.class);
            jsonReader.close();
        }
        else
        {
            Log.getLogger().info("{} does not exists inside: \"{}\"", JSON_NAME, userRoot.toAbsolutePath().toString());

            result = new StylesHeadFile();
        }
        result.checkFieldsNotNull(userRoot);

        return result;
    }

    public void write(final Path userRoot) throws IOException
    {
        final Path jsonPath = userRoot.resolve(JSON_NAME);

        final Writer writer = Files.newBufferedWriter(jsonPath);
        BetterGSON.withCreate(Additions.PATH, Additions.FORMAT).toJson(this, writer);
        writer.close();
    }

    public static class StyleEntry
    {
        private UUID schemaServerId;

        public void setSchemaServerId(final UUID schemaServerId)
        {
            this.schemaServerId = schemaServerId;
        }

        public UUID getSchemaServerId()
        {
            return schemaServerId;
        }
    }
}
