package com.ldtteam.structurize.management.schemaserver.iodiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.Tuple;

public class StylesSnapshot
{
    private final Map<String, List<Tuple<UUID, String>>> styles;

    public StylesSnapshot()
    {
        this(new HashMap<>());
    }

    private StylesSnapshot(final Map<String, List<Tuple<UUID, String>>> styles)
    {
        this.styles = styles;
    }

    public static StylesSnapshot of(final Map<String, List<Tuple<UUID, String>>> styles)
    {
        return new ImmutableStylesSnapshot(styles);
    }

    public void applyDiff(final StylesSnapshotDiff snapshotDiff)
    {
        for (final StylesSnapshotDiff.Diff fileDiff : snapshotDiff)
        {
            switch (fileDiff.getStatus())
            {
                case ADDED:
                case UPDATED:
                    fileToLastModified.put(fileDiff.getPath(), fileDiff.getNewLastModifiedTime());
                    break;

                case REMOVED:
                    fileToLastModified.remove(fileDiff.getPath());
                    break;

                default:
                    throw new RuntimeException("Missing file diff type.");
            }
        }
    }

    public boolean contains(final String path)
    {
        return fileToLastModified.containsKey(path);
    }

    public int fileLastModifiedDiff(final String path, final long lastModifiedTime)
    {
        return Long.signum(lastModifiedTime - getFileLastModifiedTime(path));
    }

    public boolean addFileLastModifiedTime(final String path, final long lastModifiedTime)
    {
        final boolean overwritten = fileToLastModified.containsKey(path);

        fileToLastModified.put(path, lastModifiedTime);

        return overwritten;
    }

    public long getFileLastModifiedTime(final String path)
    {
        if (!fileToLastModified.containsKey(path))
        {
            throw new IllegalStateException("FileTreeSnapshot does not contain: " + path);
        }

        return fileToLastModified.get(path);
    }

    public List<String> getMissingFilesIn(final Collection<String> paths)
    {
        final List<String> result = new ArrayList<>();

        for (final String path : fileToLastModified.keySet())
        {
            if (!paths.contains(path))
            {
                result.add(path);
            }
        }

        return result;
    }

    public static class ImmutableStylesSnapshot extends StylesSnapshot
    {
        private ImmutableStylesSnapshot(final Map<String, List<Tuple<UUID, String>>> styles)
        {
            super(styles);
        }

        @Override
        public boolean addFileLastModifiedTime(final String path, final long lastModifiedTime)
        {
            throw new UnsupportedOperationException();
        }
    }
}
