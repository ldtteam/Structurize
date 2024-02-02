package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.network.messages.NotifyClientAboutStructurePacksMessage;
import com.ldtteam.structurize.network.messages.TransferStructurePackToClient;
import com.ldtteam.structurize.util.IOPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.ldtteam.structurize.api.util.constant.Constants.*;

/**
 * Here we load the structure packs on the server side.
 */
public class ServerStructurePackLoader
{
    /**
     * Different states of the client structure loading progress.
     */
    public enum ServerLoadingState
    {
        UNINITIALIZED,
        LOADING,
        FINISHED_LOADING,
        FINISHED_SYNCING
    }

    /**
     * Bytebuffers that have to be synced to the client. Each buffer represents one structurepack.
     */
    private static ConcurrentLinkedQueue<PackagedPack> messageSendTasks = new ConcurrentLinkedQueue<>();

    /**
     * Map of the client sync requests that have to be handled yet.
     */
    private static Map<UUID, Map<String, Integer>> clientSyncRequests = new HashMap<>();

    /**
     * Set after the client finished loading the schematics.
     */
    public static volatile ServerStructurePackLoader.ServerLoadingState loadingState = ServerLoadingState.UNINITIALIZED;

    /**
     * Called on server mod construction.
     */
    public static void onServerStarting()
    {
        loadingState = ServerLoadingState.LOADING;
        final List<Path> modPaths = new ArrayList<>();
        final List<String> modList = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods())
        {
            modPaths.add(mod.getOwningFile().getFile().findResource(BLUEPRINT_FOLDER, mod.getModId()));
            modList.add(mod.getModId());
        }

        final Path gameFolder = new File(".").toPath();

        IOPool.execute(() ->
        {
            try
            {
                // This loads from the jar
                for (final Path modPath : modPaths)
                {
                    try
                    {
                        try (final Stream<Path> paths = Files.list(modPath))
                        {
                            paths.forEach(element -> StructurePacks.discoverPackAtPath(element, true, modList, false, modPath.toString().split("/")[1]));
                        }
                    }
                    catch (IOException e)
                    {
                        Log.getLogger().warn("Failed loading packs from mod path: " + modPath.toString());
                    }
                }

                // Now we load from the main folder.
                try
                {
                    try (final Stream<Path> paths = Files.list(gameFolder.resolve(BLUEPRINT_FOLDER)))
                    {
                        paths.forEach(element -> StructurePacks.discoverPackAtPath(element, false, modList, false, LOCAL));
                    }
                }
                catch (IOException e)
                {
                    Log.getLogger().warn("Failed loading packs from main folder path: " + gameFolder);
                }

                // Now we load from the client caches.
                try
                {
                    try (final Stream<Path> paths = Files.list(gameFolder.resolve(BLUEPRINT_FOLDER).resolve(CLIENT_FOLDER)))
                    {
                        paths.forEach(element ->
                        {
                            try
                            {
                                try (final Stream<Path> subPaths = Files.list(element))
                                {
                                    subPaths.forEach(subElement -> StructurePacks.discoverPackAtPath(subElement, false, modList, true, LOCAL));
                                }
                            }
                            catch (IOException e)
                            {
                                Log.getLogger().warn("Failed loading client pack from folder path: " + element);
                            }
                        });
                    }
                }
                catch (IOException e)
                {
                    Log.getLogger().warn("Failed loading client packs from main folder path: " + gameFolder);
                }

                Log.getLogger().warn("Finished discovering Server Structure packs");

                for (final StructurePackMeta pack : StructurePacks.getPackMetas())
                {
                    if (!pack.isImmutable())
                    {
                        // Request to remote schematic server for update if necessary.
                    }
                }
                loadingState = ServerLoadingState.FINISHED_LOADING;
            }
            catch (Throwable t)
            {
                Log.getLogger().error("schematic loading from disk failed, please report this on the mods issue tracker!", t);
            }
            finally
            {
                StructurePacks.setFinishedLoading();
            }
        });
    }

    /**
     * Called on client sync attempt.
     * @param clientStructurePacks the client structure packs.
     */
    public static void onClientSyncAttempt(final Map<String, Integer> clientStructurePacks, final ServerPlayer player)
    {
        if (loadingState == ServerLoadingState.UNINITIALIZED)
        {
            Network.getNetwork().sendToPlayer(new NotifyClientAboutStructurePacksMessage(Collections.emptyMap()), player);
            // Noop Single Player, Nothing to do here.
            return;
        }

        if (loadingState == ServerLoadingState.FINISHED_LOADING)
        {
            handleClientUpdate(clientStructurePacks, player);
        }
        else
        {
            clientSyncRequests.put(player.getUUID(), clientStructurePacks);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide())
        {
            if (event.level.getGameTime() % 20 == 0 && loadingState == ServerLoadingState.FINISHED_LOADING && !clientSyncRequests.isEmpty())
            {
                loadingState = ServerLoadingState.FINISHED_SYNCING;
                for (final Map.Entry<UUID, Map<String, Integer>> entry : clientSyncRequests.entrySet())
                {
                    final ServerPlayer player = (ServerPlayer) event.level.getPlayerByUUID(entry.getKey());
                    if (player != null)
                    {
                        handleClientUpdate(entry.getValue(), player);
                    }
                }
                clientSyncRequests.clear();
            }

            if (!messageSendTasks.isEmpty())
            {
                final PackagedPack packData = messageSendTasks.poll();
                final ServerPlayer player = (ServerPlayer) event.level.getPlayerByUUID(packData.player);
                // If the player logged off, we can just skip.
                if (player != null)
                {
                    Network.getNetwork().sendToPlayer(new TransferStructurePackToClient(packData.structurePack, packData.buf, packData.eol), player);
                }
            }
        }
    }

    /**
     * Handle the client update for a given player and their packs.
     * @param clientStructurePacks the client structure packs.
     * @param player the player.
     */
    private static void handleClientUpdate(final Map<String, Integer> clientStructurePacks, final ServerPlayer player)
    {
        final UUID uuid = player.getUUID();
        final Map<String, StructurePackMeta> missingPacks = new HashMap<>();
        final Map<String, StructurePackMeta> packsToSync = new HashMap<>();

        for (final StructurePackMeta pack : StructurePacks.getPackMetas())
        {
            if (!pack.isImmutable())
            {
                if (clientStructurePacks.getOrDefault(pack.getName(), -1) != pack.getVersion())
                {
                    missingPacks.put(pack.getName(), pack);
                }
                else
                {
                    packsToSync.put(pack.getName(), pack);
                }
            }
        }

        packsToSync.putAll(missingPacks);
        Network.getNetwork().sendToPlayer(new NotifyClientAboutStructurePacksMessage(packsToSync), player);

        IOPool.execute(() -> {
            int index = 1;
            for (final StructurePackMeta pack : new ArrayList<>(missingPacks.values()))
            {
                final ByteBuf outputBuf = zipPack(pack.getPath());
                if (outputBuf != null)
                {
                    messageSendTasks.add(new PackagedPack(pack.getName(), uuid, outputBuf, index == missingPacks.size()));
                }
                index++;
            }
        });
    }

    /**
     * ZIP up the data from the pack path and put it into a bytebuffer.
     * @param sourcePath the source path.
     * @return the bytebuffer to serialize it on the network.
     */
    private static ByteBuf zipPack(final Path sourcePath)
    {
        final ByteBuf buffer = Unpooled.buffer();
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            ZipOutputStream zos = new ZipOutputStream(stream);
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>()
            {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
                {
                    if (!sourcePath.equals(dir))
                    {
                        zos.putNextEntry(new ZipEntry(sourcePath.relativize(dir) + File.separator));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
                {
                    zos.putNextEntry(new ZipEntry(sourcePath.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e)
        {
            Log.getLogger().warn("Unable to ZIP up: " + sourcePath.toString());
            return null;
        }
        return buffer;
    }

    /**
     * Data required to send a structure pack to a client.
     */
    private static class PackagedPack
    {
        /**
         * The unique name of the structure pack.
         */
        private final String structurePack;

        /**
         * The player UUID to send it to.
         */
        private final UUID player;

        /**
         * The zipped data.
         */
        private final ByteBuf buf;

        /**
         * Is this the EOL package? (true if so).
         */
        private final boolean eol;

        /**
         * Create a new pack.
         * @param structurePack the name of the pack.
         * @param player the player to send it to.
         * @param buf the zipped data.
         * @param eol last message.
         */
        public PackagedPack(final String structurePack, final UUID player, final ByteBuf buf, final boolean eol)
        {
            this.structurePack = structurePack;
            this.player = player;
            this.buf = buf;
            this.eol = eol;
        }
    }
}
