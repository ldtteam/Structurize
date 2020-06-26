package com.ldtteam.structurize.management.schemaserver.iodiff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileTreeSnapshot
{
    private final Map<String, Long> fileToLastModified;

    public FileTreeSnapshot()
    {
        this(new HashMap<>());
    }

    private FileTreeSnapshot(final Map<String, Long> fileToLastModified)
    {
        this.fileToLastModified = fileToLastModified;
    }

    public static FileTreeSnapshot of(final Map<String, Long> fileToLastModified)
    {
        return new ImmutableFileTreeSnapshot(fileToLastModified);
    }

    public void applyDiff(final FileTreeSnapshotDiff snapshotDiff)
    {
        for (final FileTreeSnapshotDiff.Diff fileDiff : snapshotDiff)
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

    public static class ImmutableFileTreeSnapshot extends FileTreeSnapshot
    {
        private ImmutableFileTreeSnapshot(final Map<String, Long> fileToLastModified)
        {
            super(fileToLastModified);
        }

        @Override
        public boolean addFileLastModifiedTime(final String path, final long lastModifiedTime)
        {
            throw new UnsupportedOperationException();
        }
    }
}
