package com.ldtteam.structurize.management.schemaserver.fm;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.schemaserver.iodiff.FileTreeSnapshot;
import com.ldtteam.structurize.management.schemaserver.iodiff.FileTreeSnapshotDiff;
import com.ldtteam.structurize.management.schemaserver.utils.BetterGSON;
import com.ldtteam.structurize.management.schemaserver.utils.BetterGSON.Additions;

public class DataHeadFile
{
    public static final String JSON_NAME = "data.json";

    private Map<String, DataEntry> files;
    private Path dataFolder;

    private DataHeadFile()
    {
    }

    public Path getDataFolder()
    {
        return dataFolder;
    }

    public FileTreeSnapshot getFileSnapshot()
    {
        return files.isEmpty() ? new FileTreeSnapshot()
            : FileTreeSnapshot.of(files.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Long.valueOf(e.getValue().getLastTimeModified()))));
    }

    public void processSnapshotDiff(final FileTreeSnapshotDiff snapshotDiff, final DataActionProcessor actionProcessor) throws IOException
    {
        for (final FileTreeSnapshotDiff.Diff fileDiff : snapshotDiff)
        {
            final DataEntry oldEntry = files.containsKey(fileDiff.getPath()) ? files.get(fileDiff.getPath()) : new DataEntry();
            final DataEntry newEntry = actionProcessor.apply(fileDiff, oldEntry);
            if (newEntry == null)
            {
                files.remove(fileDiff.getPath());
            }
            else
            {
                files.put(fileDiff.getPath(), newEntry);
            }
        }
    }

    private void checkFieldsNotNull(final Path userRoot)
    {
        if (files == null)
        {
            files = new HashMap<>();
        }
        if (dataFolder == null)
        {
            dataFolder = userRoot.resolve("data");
        }
    }

    public static DataHeadFile read(final Path userRoot) throws IOException
    {
        final DataHeadFile result;
        final Path jsonPath = userRoot.resolve(JSON_NAME);

        if (Files.exists(jsonPath))
        {
            final JsonReader jsonReader = new JsonReader(Files.newBufferedReader(jsonPath));
            result = BetterGSON.withCreate(Additions.PATH).fromJson(jsonReader, DataHeadFile.class);
            jsonReader.close();
        }
        else
        {
            Log.getLogger().info("{} does not exists inside: \"{}\"", JSON_NAME, userRoot.toAbsolutePath().toString());

            result = new DataHeadFile();
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

    public static class DataEntry
    {
        private UUID schemaServerId;
        private long lastTimeModified;

        public void setSchemaServerId(final UUID schemaServerId)
        {
            this.schemaServerId = schemaServerId;
        }

        public void setLastTimeModified(final long lastTimeModified)
        {
            this.lastTimeModified = lastTimeModified;
        }

        public UUID getSchemaServerId()
        {
            return schemaServerId;
        }

        public long getLastTimeModified()
        {
            return lastTimeModified;
        }
    }

    @FunctionalInterface
    public static interface DataActionProcessor
    {
        DataEntry apply(FileTreeSnapshotDiff.Diff t, DataEntry u) throws IOException;
    }
}
