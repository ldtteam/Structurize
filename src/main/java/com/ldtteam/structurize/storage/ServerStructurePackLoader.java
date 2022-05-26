package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
        LOADING,
        FINISHED_LOADING,
        FINISHED_SYNCING
    }

    /**
     * Map of the client sync requests that have to be handled yet.
     */
    private static Map<UUID, Map<String, Integer>> clientSyncRequests = new HashMap<>();

    /**
     * Set after the client finished loading the schematics.
     */
    public static volatile ServerStructurePackLoader.ServerLoadingState loadingState = ServerStructurePackLoader.ServerLoadingState.LOADING;

    /**
     * Called on server mod construction.
     */
    public static void onServerStarting()
    {
        final List<Path> modPaths = new ArrayList<>();
        final List<String> modList = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods())
        {
            modPaths.add(mod.getOwningFile().getFile().findResource("structures", mod.getModId()));
            modList.add(mod.getModId());
        }

        final Path gameFolder = ServerLifecycleHooks.getCurrentServer().getServerDirectory().toPath();

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
                Files.list(gameFolder.resolve("structures")).forEach(element -> StructurePacks.discoverPackAtPath(element, false, modList));
            }
            catch (IOException e)
            {
                Log.getLogger().warn("Failed loading packs from main folder path: " + gameFolder.toString());
            }

            Log.getLogger().warn("Finished discovering Structure packs");

            for (final StructurePack pack : StructurePacks.packs.values())
            {
                if (!pack.isImmutable())
                {
                    // Request to remote schematic server for update if necessary.
                }
            }
            loadingState = ServerLoadingState.FINISHED_LOADING;
        });
    }

    /**
     * Called on client sync attempt.
     * @param clientStructurePacks the client structure packs.
     */
    public static void onClientSyncAttempt(final Map<String, Integer> clientStructurePacks, final Player player)
    {
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
        if (event.phase == TickEvent.Phase.END && event.world.isClientSide() && event.world.getGameTime() % 20 == 0 && loadingState == ServerLoadingState.FINISHED_LOADING && !clientSyncRequests.isEmpty())
        {
            loadingState = ServerLoadingState.FINISHED_SYNCING;
            for (final Map.Entry<UUID, Map<String, Integer>> entry : clientSyncRequests.entrySet())
            {
                final Player player = event.world.getPlayerByUUID(entry.getKey());
                if (player != null)
                {
                    handleClientUpdate(entry.getValue(), player);
                }
            }
        }
    }

    /**
     * Handle the client update for a given player and their packs.
     * @param clientStructurePacks the client structure packs.
     * @param player the player.
     */
    private static void handleClientUpdate(final Map<String, Integer> clientStructurePacks, final Player player)
    {
        final List<String> packsToSync = new ArrayList<>();
        for (final Map.Entry<String, StructurePack> entry : StructurePacks.packs.entrySet())
        {
            if (!entry.getValue().isImmutable() && clientStructurePacks.getOrDefault(entry.getKey(), -1) != entry.getValue().getVersion())
            {
                packsToSync.add(entry.getKey());
            }
        }

        //todo two messages:
        // a) Server structurePack state (client can then already delete all packs that mismatch, or are missing (only delete outdated we're gonna replace soon, deactivate from list the others)
        // b) Then one message per missing/outdated style. Collect this on a seperate tick, and then put those into a buffer to be sent over later.
        Network.getNetwork().sendToPlayer(data, player);
        //todo send over.
    }
}
