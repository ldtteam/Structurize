package com.ldtteam.structurize.util;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.BUFFER_SIZE;
import static com.ldtteam.structurize.api.util.constant.Suppression.RESOURCES_SHOULD_BE_CLOSED;
import static com.ldtteam.structurize.management.Structures.SCHEMATIC_EXTENSION_NEW;

/**
 * Utilities for Structure loading
 */
public final class StructureLoadingUtils
{
    /**
     * The list of origin folders.
     */
    public static List<String> originFolders = new ArrayList<>();

    /**
     * Private constructor to hide public one.
     */
    private StructureLoadingUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get an input stream for a schematic within a specif folder.
     *
     * @param folder        where to load it from.
     * @param structureName name of the structure to load.
     * @return the input stream or null
     */
    @Nullable
    private static InputStream getStreamFromFolder(@Nullable final File folder, final String structureName)
    {
        if (folder == null)
        {
            return null;
        }
        final File blueprintFile = new File(folder.getPath() + "/" + structureName + SCHEMATIC_EXTENSION_NEW);
        try
        {
            if (folder.exists())
            {
                //We need to check that we stay within the correct folder
                if (!blueprintFile.toURI().normalize().getPath().startsWith(folder.toURI().normalize().getPath()))
                {
                    Log.getLogger().error("Structure: Illegal structure name \"" + structureName + "\"");
                    return null;
                }
                else if (blueprintFile.exists())
                {
                    return new FileInputStream(blueprintFile);
                }
            }
        }
        catch (final FileNotFoundException e)
        {
            //we should will never go here
            Log.getLogger().error("Structure.getStreamFromFolder", e);
        }
        return null;
    }

    /**
     * get a input stream for a schematic from jar.
     *
     * @param structureName name of the structure to load from the jar.
     * @return the input stream or null
     */
    private static List<InputStream> getStreamsFromJar(final String structureName)
    {
        final List<InputStream> streamsFromJar = new ArrayList<>();
        for (final String origin : originFolders)
        {
            streamsFromJar.add(MinecraftServer.class.getResourceAsStream("/assets/" + origin + '/' + structureName + SCHEMATIC_EXTENSION_NEW));
        }
        return streamsFromJar;
    }

    /**
     * Convert an InputStream into and array of bytes.
     *
     * @param stream to be converted to bytes array
     * @return the array of bytes, array is size 0 when the stream is null
     */
    public static byte[] getStreamAsByteArray(final InputStream stream)
    {
        if (stream == null)
        {
            Log.getLogger().info("Structure.getStreamAsByteArray: stream is null this should not happen");
            return new byte[0];
        }
        try
        {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            final byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = stream.read(data, 0, data.length)) != -1)
            {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
        catch (@NotNull final IOException e)
        {
            Log.getLogger().trace(e);
        }
        return new byte[0];
    }

    /**
     * get a InputStream for a give structureName.
     * <p>
     * Look into the following director (in order):
     * - scan
     * - cache
     * - schematics folder
     * - jar
     * It should be the exact opposite that the way used to build the list.
     * <p>
     * Suppressing Sonar Rule squid:S2095
     * This rule enforces "Close this InputStream"
     * But in this case the rule does not apply because
     * We are returning the stream and that is reasonable
     *
     * @param structureName name of the structure to load
     * @return the input stream or null
     */
    @SuppressWarnings(RESOURCES_SHOULD_BE_CLOSED)
    @Nullable
    public static InputStream getStream(final String structureName)
    {
        final StructureName sn = new StructureName(structureName);
        InputStream inputstream = null;
        if (Structures.SCHEMATICS_CACHE.equals(sn.getPrefix()))
        {
            for (final File cachedFile : StructureLoadingUtils.getCachedSchematicsFolders())
            {
                final InputStream stream = StructureLoadingUtils.getStreamFromFolder(cachedFile, structureName);
                if (stream != null)
                {
                    return stream;
                }
            }
        }
        else if (Structures.SCHEMATICS_SCAN.equals(sn.getPrefix()))
        {
            for (final File cachedFile : StructureLoadingUtils.getClientSchematicsFolders())
            {
                final InputStream stream = StructureLoadingUtils.getStreamFromFolder(cachedFile, structureName);
                if (stream != null)
                {
                    return stream;
                }
            }
        }
        else if (!Structures.SCHEMATICS_PREFIX.equals(sn.getPrefix()))
        {
            return null;
        }
        else
        {
            //Look in the folder first
            inputstream = StructureLoadingUtils.getStreamFromFolder(Structurize.proxy.getSchematicsFolder(), structureName);
            if (inputstream == null && !Structurize.getConfig().getServer().ignoreSchematicsFromJar.get())
            {
                for (final InputStream stream : StructureLoadingUtils.getStreamsFromJar(structureName))
                {
                    if (stream != null)
                    {
                        inputstream = stream;
                    }
                }
            }
        }

        return inputstream;
    }

    /**
     * Get the file representation of the cached schematics' folder.
     *
     * @return the folder for the cached schematics
     */
    public static List<File> getCachedSchematicsFolders()
    {
        final List<File> cachedSchems = new ArrayList<>();
        for (final String origin : originFolders)
        {
            if (ServerLifecycleHooks.getCurrentServer() == null)
            {
                if (Manager.getServerUUID() != null)
                {
                    cachedSchems.add(new File(Minecraft.getInstance().gameDir, origin + "/" + Manager.getServerUUID()));
                }
                else
                {
                    Log.getLogger().error("Manager.getServerUUID() => null this should not happen");
                    return null;
                }
            }
            else
            {
                cachedSchems.add(new File(ServerLifecycleHooks.getCurrentServer().getDataDirectory() + "/" + Constants.MOD_ID));
            }
        }
        return cachedSchems;
    }

    /**
     * get the schematic folder for the client.
     *
     * @return the client folder.
     */
    public static List<File> getClientSchematicsFolders()
    {
        final List<File> clientSchems = new ArrayList<>();
        for (final String origin : originFolders)
        {
            clientSchems.add(new File(Minecraft.getInstance().gameDir, origin));
        }
        return clientSchems;
    }
}
