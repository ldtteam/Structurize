package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.linksession.LinkSessionManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utils for saving data not saved into capabilities
 */
public final class BackUpHelper
{
    private static final String FILENAME_EXT_OLD = ".old";
    private static final String FILENAME_EXT_DAT = ".dat";
    private static final String FILENAME_STRUCTURIZE_PATH = "structurize";
    private static final String FILENAME_LINKSESSION = "_linksession";
    // ISO_LOCAL_DATE_TIME with dots
    private static final DateTimeFormatter BACKUP_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH.mm.ss");

    /**
     * Private constructor to hide implicit one
     */
    private BackUpHelper()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Save all LinkSessions
     */
    public static void saveLinkSessionManager()
    {
        @NotNull final CompoundNBT data = LinkSessionManager.INSTANCE.serializeNBT();
        @NotNull final File file = getSaveLocation(FILENAME_LINKSESSION);

        cycleNewBackup(FILENAME_LINKSESSION, 2);  // TODO: make configurable
        saveNBTToPath(file, data);
    }

    /**
     * Load all LinkSessions if found
     */
    public static void loadLinkSessionManager()
    {
        @NotNull final File file = getSaveLocation(FILENAME_LINKSESSION);
        @NotNull final CompoundNBT data = loadNBTFromPath(file);

        if(data != null)
        {
            LinkSessionManager.INSTANCE.deserializeNBT(data);
        }
    }

    /**
     * Convert existing file and cycle backup files up to N files
     * 
     * @param additionalPath additional path in the file name
     * @param cycleUpToNFiles how many backup files should be present at max
     */
    private static void cycleNewBackup(@NotNull final String additionalPath, @NotNull final int cycleUpToNFiles)
    {
        @NotNull final File current = getSaveLocation(additionalPath);
        @NotNull final File newBackup = getBackupSaveLocation(additionalPath, LocalDateTime.now());
        if(!current.exists() || getSaveDir().list() == null)
        {
            return;
        }
        current.renameTo(newBackup);
        @NotNull final Supplier<Stream<String>> allBackups = () -> Stream.of(getSaveDir().list())
            .filter(fileName -> fileName.contains(FILENAME_EXT_OLD) && fileName.contains(FILENAME_STRUCTURIZE_PATH + additionalPath));

        if(allBackups.get().count() > cycleUpToNFiles)
        {
            final AtomicInteger toRemove = new AtomicInteger((int) allBackups.get().count() - cycleUpToNFiles);
            allBackups.get()
                .map(fileName -> getTimestampFromBackup(additionalPath, fileName))
                .sorted() // from earliest to latest
                .filter(a -> toRemove.getAndDecrement() > 0)
                .map(datetime -> getBackupSaveLocation(additionalPath, datetime))
                .forEach(fileToRemove -> fileToRemove.delete());
        }
    }

    /**
     * Getter for a save mod directory under world/save directory
     * 
     * @return File: mod directory
     */
    @NotNull
    private static File getSaveDir()
    {
        return ServerLifecycleHooks.getCurrentServer().func_240776_a_(new FolderName(FILENAME_STRUCTURIZE_PATH)).toFile();
    }

    /**
     * Getter for a casual file location
     *
     * @param additionalPath additional path in the file name
     * @return File: casual save file
     */
    @NotNull
    private static File getSaveLocation(@NotNull final String additionalPath)
    {
        return new File(getSaveDir(), FILENAME_STRUCTURIZE_PATH + additionalPath + FILENAME_EXT_DAT);
    }

    /**
     * Getter for a backup file location
     *
     * @param additionalPath additional path in the file name
     * @return File: backup save file
     */
    @NotNull
    private static File getBackupSaveLocation(@NotNull final String additionalPath, @NotNull final LocalDateTime date)
    {
        return new File(getSaveDir(), 
            String.format(FILENAME_STRUCTURIZE_PATH + additionalPath + "-%s" + FILENAME_EXT_DAT + FILENAME_EXT_OLD, BACKUP_TIMESTAMP.format(date)));
    }

    /**
     * Parser for a date time from file name
     * 
     * @param additionalPath additional path in the file name
     * @param fileName to parse from
     * @return LocalDateTime: interpretation of date time in the file name
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     */
    @NotNull
    private static LocalDateTime getTimestampFromBackup(@NotNull final String additionalPath, @NotNull final String fileName)
    {
        return LocalDateTime.parse(
            fileName.replace(FILENAME_STRUCTURIZE_PATH + additionalPath + "-", "").replace(FILENAME_EXT_DAT + FILENAME_EXT_OLD, ""),
            BACKUP_TIMESTAMP);
    }

    /**
     * Save an CompoundNBT to a file.  Does so in a safe manner using an
     * intermediate tmp file.
     *
     * @param file     The destination file to write the data to.
     * @param compound The CompoundNBT to write to the file.
     */
    public static void saveNBTToPath(@Nullable final File file, @NotNull final CompoundNBT compound)
    {
        try
        {
            if (file != null)
            {
                file.getParentFile().mkdir();
                safeWrite(compound, file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when saving data into external file!", exception);
        }
    }

    /**
     * Load a file and return the data as an CompoundNBT.
     *
     * @param file The path to the file.
     * @return the data from the file as an CompoundNBT, or null.
     */
    public static CompoundNBT loadNBTFromPath(@Nullable final File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when loading data from external file!", exception);
        }
        return null;
    }

    public static void safeWrite(final CompoundNBT compound, final File fileIn) throws IOException
    {
        final File file1 = new File(fileIn.getAbsolutePath() + "_tmp");
        if (file1.exists())
        {
            file1.delete();
        }

        CompressedStreamTools.write(compound, file1);
        if (fileIn.exists())
        {
            fileIn.delete();
        }

        if (fileIn.exists())
        {
            throw new IOException("Failed to delete " + fileIn);
        }

        else
        {
            file1.renameTo(fileIn);
        }
    }
}
