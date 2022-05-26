package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.network.messages.NotifyServerAboutStructurePacks;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Client side structure pack discovery.
 */
public class ClientStructurePackLoader
{
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
                Files.list(gameFolder.resolve("structures")).forEach(element -> StructurePacks.discoverPackAtPath(element, false, modList));
            }
            catch (IOException e)
            {
                Log.getLogger().warn("Failed loading packs from main folder path: " + gameFolder.toString());
            }

            Log.getLogger().warn("Finished discovering Structure packs");

            loadingState = ClientLoadingState.FINISHED_LOADING;
        });
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END && event.world.isClientSide() && event.world.getGameTime() % 20 == 0 && loadingState == ClientLoadingState.FINISHED_LOADING)
        {
            loadingState = ClientLoadingState.SYNCING;
            Network.getNetwork().sendToServer(new NotifyServerAboutStructurePacks(StructurePacks.packs));
        }
    }

    public static void onServerSync()
    {
        // todo (If server doesn't know about a pack, client removes from index).
        // todo, server sends us two things
        // a) Pack String/Version list (of server packs) to disable some on the client.
        // b) All the files of the missing packs/outdated packs.
        // so for b we have to
        // 1) Delete the outdated packs
        // 2) Add the outdated pack data.
        // 3) Add the updated/new packs to the map.
    }
}
