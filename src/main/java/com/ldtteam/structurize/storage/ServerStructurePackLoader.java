package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.network.messages.NotifyClientAboutStructurePacks;
import com.ldtteam.structurize.network.messages.TransferStructurePackToClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;

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

        Util.ioPool().execute(() ->
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
                Files.list(gameFolder.resolve(BLUEPRINT_FOLDER)).forEach(element -> StructurePacks.discoverPackAtPath(element, false, modList));
            }
            catch (IOException e)
            {
                Log.getLogger().warn("Failed loading packs from main folder path: " + gameFolder.toString());
            }

            Log.getLogger().warn("Finished discovering Server Structure packs");

            for (final StructurePackMeta pack : StructurePacks.packMetas.values())
            {
                if (!pack.isImmutable())
                {
                    // Request to remote schematic server for update if necessary.
                }
            }
            loadingState = ServerLoadingState.FINISHED_LOADING;
            StructurePacks.finishedLoading = true;
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
            Network.getNetwork().sendToPlayer(new NotifyClientAboutStructurePacks(Collections.emptyMap()), player);
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
    public static void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide())
        {
            if (event.world.getGameTime() % 20 == 0 && loadingState == ServerLoadingState.FINISHED_LOADING && !clientSyncRequests.isEmpty())
            {
                loadingState = ServerLoadingState.FINISHED_SYNCING;
                for (final Map.Entry<UUID, Map<String, Integer>> entry : clientSyncRequests.entrySet())
                {
                    final ServerPlayer player = (ServerPlayer) event.world.getPlayerByUUID(entry.getKey());
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
                final ServerPlayer player = (ServerPlayer) event.world.getPlayerByUUID(packData.player);
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

        for (final Map.Entry<String, StructurePackMeta> entry : StructurePacks.packMetas.entrySet())
        {
            if (!entry.getValue().isImmutable())
            {
                if (clientStructurePacks.getOrDefault(entry.getKey(), -1) != entry.getValue().getVersion())
                {
                    missingPacks.put(entry.getKey(), entry.getValue());
                }
                else
                {
                    packsToSync.put(entry.getKey(), entry.getValue());
                }
            }
        }

        packsToSync.putAll(missingPacks);
        Network.getNetwork().sendToPlayer(new NotifyClientAboutStructurePacks(packsToSync), player);

        Util.ioPool().execute(() -> {
            int index = 0;
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
                        zos.putNextEntry(new ZipEntry(sourcePath.relativize(dir).toString() + "/"));
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
