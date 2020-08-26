package com.ldtteam.structurize.util;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.ldtteam.structurize.api.util.constant.Constants.BUFFER_SIZE;
import static com.ldtteam.structurize.api.util.constant.Suppression.RESOURCES_SHOULD_BE_CLOSED;
import static com.ldtteam.structurize.management.Structures.SCHEMATICS_ASSET_PATH;
import static com.ldtteam.structurize.management.Structures.SCHEMATIC_EXTENSION_NEW;

/**
 * Utilities for Structure loading
 */
public final class StructureLoadingUtils
{
    /**
     * The list of origin folders.
     */
    private static final List<String> originFolders = new ArrayList<>();
    /**
     * The list of origin mods.
     */
    private static final Map<String, ModFileInfo> originMods = new HashMap<>();
    /**
     * Allows for faster lookup during jar discover or build tool listing.
     */
    @NotNull
    private static String latestModOrigin = Constants.MOD_ID;

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
     * Adds folder to <b>folder</b> blueprint-lookup list.
     *
     * @param folder name or path
     */
    public static void addOriginFolder(final String folder)
    {
        originFolders.add(folder);
    }

    /**
     * Adds modId to <b>folder</b> blueprint-lookup list.
     * Adds modId to <b>mod</b> blueprint-lookup list only if mod is loaded and it's {@link ModFileInfo} is found.
     *
     * @param modId lowercased modId
     */
    public static void addOriginMod(final String modId)
    {
        addOriginMod(modId, ModList.get().getModFileById(modId));
    }

    /**
     * Adds modId to <b>folder</b> blueprint-lookup list.
     * Adds modId to <b>mod</b> blueprint-lookup list only if modDescriptor is not null.
     * Can be used for overrides.
     *
     * @param modId         lowercased modId
     * @param modDescriptor mod descriptor to link with given modId
     */
    public static void addOriginMod(final String modId, final ModFileInfo modDescriptor)
    {
        addOriginFolder(modId);
        if (modDescriptor != null)
        {
            originMods.put(modId, modDescriptor);
        }
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
                // We need to check that we stay within the correct folder
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
            // we should will never go here
            Log.getLogger().error("Structure.getStreamFromFolder", e);
        }
        return null;
    }

    /**
     * Tries to find given resource path in given mod.
     *
     * @param info mod descriptor
     * @param path resource path elements, without file system seperator
     * @return is if path was found, null otherwise
     */
    private static InputStream getStreamFromMod(final ModFileInfo info, final String... path)
    {
        final Path ret = info.getFile().getLocator().findPath(info.getFile(), path);
        if (Files.exists(ret))
        {
            Log.getLogger().warn("File jar resolve: {}", ret.toString());
            try
            {
                return Files.newInputStream(ret);
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("Error occured when trying to read resource from: " + info.getMods().stream(), e);
            }
        }
        return null;
    }

    /**
     * get a input stream for a schematic from jar.
     *
     * @param structureName name of the structure to load from the jar.
     * @return the input stream or null
     */
    private static InputStream getStreamFromJar(final String structureName)
    {
        final String filePath = structureName + SCHEMATIC_EXTENSION_NEW;
        Log.getLogger().warn("File jar request: {}", filePath);

        // try latest successful origin
        InputStream is = getStreamFromMod(originMods.get(latestModOrigin), SCHEMATICS_ASSET_PATH, latestModOrigin, filePath);
        if (is == null)
        {
            // try every origin except the one tested earlier
            for (final Map.Entry<String, ModFileInfo> origin : originMods.entrySet())
            {
                final String originName = origin.getKey();
                if (!originName.equals(latestModOrigin))
                {
                    is = getStreamFromMod(origin.getValue(), SCHEMATICS_ASSET_PATH, originName, filePath);
                    if (is != null)
                    {
                        latestModOrigin = originName;
                        break;
                    }
                }
            }
            Log.getLogger().warn("File jar resolve: FAILED");
        }
        return is;
    }

    /**
     * Merges behaviour of {@link #getStream(String)} and {@link #getStreamAsByteArray(InputStream)}
     *
     * @param structureName name of the structure to load
     * @return the array of bytes, array is size 0 when the stream is null
     */
    public static byte[] getByteArray(final String structureName)
    {
        final InputStream is = getStream(structureName);
        final byte[] result = getStreamAsByteArray(is);
        if(is != null)
        {
            try
            {
                is.close();
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("", e);
            }
        }
        return result;
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
            Log.getLogger().warn("", e);
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
            if (inputstream == null && !Structurize.getConfig().getCommon().ignoreSchematicsFromJar.get())
            {
                inputstream = StructureLoadingUtils.getStreamFromJar(structureName);
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

    public static List<String> getOriginfolders()
    {
        return originFolders;
    }

    public static Map<String, ModFileInfo> getOriginMods()
    {
        return originMods;
    }
}
