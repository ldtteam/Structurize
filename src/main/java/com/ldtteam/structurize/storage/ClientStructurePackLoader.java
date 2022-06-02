package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.network.messages.NotifyServerAboutStructurePacks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Client side structure pack discovery.
 */
public class ClientStructurePackLoader
{
    //todo add Md5 support in the future (stronger consistency guarantee).

    /**
     * Different states of the client structure loading progress.
     */
    public enum ClientLoadingState
    {
        LOADING,
        FINISHED_LOADING,
        SYNCING,
        FINISHED_SYNCING
    }

    /**
     * Set after the client finished loading the schematics.
     */
    public static volatile ClientLoadingState loadingState = ClientLoadingState.LOADING;

    /**
     * Called on client mod construction.
     */
    public static void onClientStarting()
    {
        final List<Path> modPaths = new ArrayList<>();
        final List<String> modList = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods())
        {
            modPaths.add(mod.getOwningFile().getFile().findResource("structures", mod.getModId()));
            modList.add(mod.getModId());
        }

        final Path gameFolder = Minecraft.getInstance().gameDirectory.toPath();

        Util.backgroundExecutor().execute(() ->
        {
            // This loads from the jar
            for (final Path modPath : modPaths)
            {
                try
                {
                    Files.list(modPath).forEach(element -> StructurePacks.discoverPackAtPath(element, true, modList));
                }
                catch (IOException e)
                {
                    Log.getLogger().warn("Failed loading packs from mod path: " + modPath.toString());
                }
            }

            // Now we load from the main folder.
            try
            {
                final Path outputPath = gameFolder.resolve("structures");
                if (!Files.exists(outputPath))
                {
                    Files.createDirectory(outputPath);
                }
                Files.list(outputPath).forEach(element -> StructurePacks.discoverPackAtPath(element, false, modList));
            }
            catch (IOException e)
            {
                Log.getLogger().warn("Failed loading packs from main folder path: " + gameFolder.toString());
            }

            Log.getLogger().warn("Finished discovering Client Structure packs");

            loadingState = ClientLoadingState.FINISHED_LOADING;
        });
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % 20 == 0 && loadingState == ClientLoadingState.FINISHED_LOADING)
            {
                loadingState = ClientLoadingState.SYNCING;
                Network.getNetwork().sendToServer(new NotifyServerAboutStructurePacks(StructurePacks.packMetas));
            }
            else if (Minecraft.getInstance().level == null && (loadingState == ClientLoadingState.SYNCING || loadingState == ClientLoadingState.FINISHED_SYNCING))
            {
                Log.getLogger().warn("Client logged off. Resetting Pack Meta and Reloading State");
                loadingState = ClientLoadingState.LOADING;
                StructurePacks.packMetas.clear();
                onClientStarting();
            }
        }
    }

    /**
     * On receiving server structure pack update.
     *
     * @param serverStructurePacks the server structure packs.
     */
    public static void onServerSyncAttempt(final Map<String, Integer> serverStructurePacks)
    {
        boolean needsChanges = false;
        for (final Map.Entry<String, StructurePack> entry : new ArrayList<>(StructurePacks.packMetas.entrySet()))
        {
            if (!entry.getValue().isImmutable())
            {
                final int version = serverStructurePacks.getOrDefault(entry.getKey(), -1);
                if (version == -1 && !Structurize.getConfig().getServer().allowPlayerSchematics.get())
                {
                    // todo on scan, client creates a specific style based on player profile automatic (random name).
                    // Don't have this pack on the server, disable.
                    StructurePacks.packMetas.remove(entry.getKey());
                }
                else if (version != entry.getValue().getVersion())
                {
                    // Version on the client is outdated. Set that we got pending changes.
                    StructurePacks.packMetas.remove(entry.getKey());
                    needsChanges = true;
                }
            }
        }

        for (final String pack : serverStructurePacks.keySet())
        {
            if (!StructurePacks.packMetas.containsKey(pack))
            {
                needsChanges = true;
                break;
            }
        }

        if (!needsChanges)
        {
            // No new packs have be synced and no updated packs have to be synced.
            loadingState = ClientLoadingState.FINISHED_SYNCING;
        }
    }

    /**
     * On reception of a new structure pack.
     *
     * @param packName the name of the structure pack.
     * @param payload the payload of the pack.
     */
    public static void onStructurePackTransfer(final String packName, final ByteBuf payload)
    {
        Log.getLogger().warn("Received Structure pack from the Server: " + packName);
        Util.backgroundExecutor().execute(() ->
        {
            final StructurePack pack = StructurePacks.packMetas.remove(packName);
            if (pack != null)
            {
                try
                {
                    FileUtils.deleteDirectory(new File(pack.getPath().toUri()));
                }
                catch (IOException e)
                {
                    Log.getLogger().warn("Error trying to delete pack: ", e);
                }
            }

            try (ZipInputStream zis = new ZipInputStream(new ByteBufInputStream(payload)))
            {
                ZipEntry zipEntry = zis.getNextEntry();
                final Path structureFolder = Minecraft.getInstance().gameDirectory.toPath().resolve("structures");
                final Path rootPath = Files.createDirectory(structureFolder.resolve(packName));

                while (zipEntry != null)
                {
                    boolean isDirectory = zipEntry.getName().endsWith(File.separator);
                    Path newPath = zipSlipProtect(zipEntry, rootPath);

                    if (isDirectory)
                    {
                        Files.createDirectories(newPath);
                    }
                    else
                    {
                        if (newPath.getParent() != null)
                        {
                            if (Files.notExists(newPath.getParent()))
                            {
                                Files.createDirectories(newPath.getParent());
                            }
                        }

                        Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();

                final List<String> modList = new ArrayList<>();
                for (IModInfo mod : ModList.get().getMods())
                {
                    modList.add(mod.getModId());
                }

                // now load what we unzipped.
                StructurePacks.discoverPackAtPath(rootPath, true, modList);
            }
            catch (final IOException ex)
            {
                Log.getLogger().error("Unable to read datapack from zip", ex);
            }

            payload.release();
        });
    }

    // protect zip slip attack
    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException
    {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir.normalize()))
        {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
}
