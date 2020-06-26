package com.ldtteam.structurize.management.schemaserver.iodiff;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.util.LanguageHandler;

public class FileWalker implements FileVisitor<Path>
{
    private final Path root;
    private final FileTreeSnapshot oldSnapshot;
    private FileTreeSnapshotDiff.Builder diffBuilder;
    private List<String> foundPaths;

    public FileWalker(final Path root, final FileTreeSnapshot oldSnapshot)
    {
        this.root = root.toAbsolutePath();
        this.oldSnapshot = oldSnapshot;
    }

    public FileTreeSnapshotDiff getDiff()
    {
        if (diffBuilder == null)
        {
            throw new IllegalStateException("Builder not ready.");
        }

        for (final String path : oldSnapshot.getMissingFilesIn(foundPaths))
        {
            diffBuilder.addRemoved(path);
        }

        final FileTreeSnapshotDiff result = diffBuilder.build();

        diffBuilder = null;
        foundPaths = null;

        return result;
    }

    private void ensureBuilder()
    {
        if (diffBuilder == null)
        {
            diffBuilder = FileTreeSnapshotDiff.getBuilder();
            foundPaths = new ArrayList<>();
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir0, final BasicFileAttributes attrs) throws IOException
    {
        ensureBuilder();

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file0, final BasicFileAttributes attrs) throws IOException
    {
        if (attrs.isRegularFile())
        {
            final Path file = file0.toAbsolutePath();
            final String relPath = root.relativize(file).toString();
            final long lastModifiedTime = attrs.lastModifiedTime().toMillis();

            ensureBuilder();
            foundPaths.add(relPath);

            if (oldSnapshot.contains(relPath))
            {
                final int diff = oldSnapshot.fileLastModifiedDiff(relPath, lastModifiedTime);
                if (diff > 0)
                {
                    diffBuilder.addUpdated(relPath, attrs.lastModifiedTime().toMillis());
                }
                else if (diff < 0)
                {
                    Log.getLogger()
                        .warn(
                            "Found older file against last snapshot - not updating, filePath: \"{}\", snapshotTime: \"{}\", fileLastModifiedTime: \"{}\"",
                            file,
                            oldSnapshot.getFileLastModifiedTime(relPath),
                            lastModifiedTime);
                    Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.filediff.old_file",
                        file,
                        oldSnapshot.getFileLastModifiedTime(relPath),
                        lastModifiedTime));
                }
            }
            else
            {
                diffBuilder.addAdded(relPath, lastModifiedTime);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException
    {
        ensureBuilder();
        Log.getLogger().error("Error encoutered during file tree walking of " + root.toString() + ": " + exc.getLocalizedMessage(), exc);
        Structurize.proxy.notifyClientOrServerOps(LanguageHandler.prepareMessage("structurize.ssioerror", exc.getLocalizedMessage()));

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException
    {
        return FileVisitResult.CONTINUE;
    }
}
