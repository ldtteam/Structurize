package com.ldtteam.structurize.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.NotifyServerAboutStructurePacksMessage;
import com.ldtteam.structurize.network.messages.SyncSettingsToServer;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.IOPool;
import com.ldtteam.structurize.util.JavaUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.ldtteam.structurize.api.util.constant.Constants.*;

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
    public static void onClientLoading()
    {
        final List<Path> modPaths = new ArrayList<>();
        final List<String> modList = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods())
        {
            modPaths.add(mod.getOwningFile().getFile().findResource(BLUEPRINT_FOLDER, mod.getModId()));
            modList.add(mod.getModId());
        }

        if (Minecraft.getInstance() == null)
        {
            // RunData
            return;
        }

        final Path gameFolder = Minecraft.getInstance().gameDirectory.toPath();

        IOPool.execute(() ->
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
                final Path outputPath = gameFolder.resolve(BLUEPRINT_FOLDER);
                if (!Files.exists(outputPath))
                {
                    Files.createDirectory(outputPath);
                }

                final Path clientPackPath = outputPath.resolve(Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US));
                if (!Files.exists(clientPackPath))
                {
                    Files.createDirectory(clientPackPath);
                    Files.createDirectory(clientPackPath.resolve(SCANS_FOLDER));
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("version", 1);
                    jsonObject.addProperty("pack-format", 1);
                    jsonObject.addProperty("desc", "This is your local Structurepack. This is where all your scans go.");
                    final JsonArray authorArray = new JsonArray();
                    authorArray.add(Minecraft.getInstance().getUser().getName());
                    jsonObject.add("authors", authorArray);
                    final JsonArray modsArray = new JsonArray();
                    modsArray.add(Constants.MOD_ID);
                    jsonObject.add("mods", modsArray);
                    jsonObject.addProperty("name", Minecraft.getInstance().getUser().getName());
                    jsonObject.addProperty("icon",  "");

                    Files.write(clientPackPath.resolve("pack.json"), jsonObject.toString().getBytes());
                }

                try (final Stream<Path> paths = Files.list(outputPath))
                {
                    paths.forEach(element -> StructurePacks.discoverPackAtPath(element, false, modList, false, LOCAL));
                }
            }
            catch (IOException e)
            {
                Log.getLogger().warn("Failed loading packs from main folder path: " + gameFolder.toString());
            }

            Log.getLogger().warn("Finished discovering Client Structure packs");

            loadingState = ClientLoadingState.FINISHED_LOADING;
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onWorldTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (Minecraft.getInstance().level != null && loadingState == ClientLoadingState.FINISHED_LOADING)
            {
                if (Minecraft.getInstance().isSingleplayer())
                {
                    loadingState = ClientLoadingState.FINISHED_SYNCING;
                    StructurePacks.setFinishedLoading();
                    if (StructurePacks.selectedPack == null && !StructurePacks.getPackMetas().isEmpty())
                    {
                        StructurePacks.selectedPack = StructurePacks.getPackMetas().iterator().next();
                    }
                    return;
                }

                loadingState = ClientLoadingState.SYNCING;
                new NotifyServerAboutStructurePacksMessage(StructurePacks.getPackMetas()).sendToServer();
            }
            else if (Minecraft.getInstance().level == null && (loadingState == ClientLoadingState.SYNCING || loadingState == ClientLoadingState.FINISHED_SYNCING))
            {
                Log.getLogger().warn("Client logged off. Resetting Pack Meta and Reloading State");
                loadingState = ClientLoadingState.LOADING;
                StructurePacks.clearPacks();
                RenderingCache.clear();
                onClientLoading();
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
        new SyncSettingsToServer().sendToServer();

        if (serverStructurePacks.isEmpty())
        {
            // Most likely single player. Skip.
            loadingState = ClientLoadingState.FINISHED_SYNCING;
            StructurePacks.setFinishedLoading();
            if (StructurePacks.selectedPack == null && !StructurePacks.getPackMetas().isEmpty())
            {
                StructurePacks.selectedPack = StructurePacks.getPackMetas().iterator().next();
            }
            return;
        }
        
        if (serverStructurePacks.containsKey(Minecraft.getInstance().player.getGameProfile().getName()))
        {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("structurize.pack.equaluser.error"));
        }

        boolean needsChanges = false;
        for (final StructurePackMeta pack : StructurePacks.getPackMetas())
        {
            if (!pack.isImmutable())
            {
                final int version = serverStructurePacks.getOrDefault(pack.getName(), -1);
                if (version == -1)
                {
                    if (!Structurize.getConfig().getServer().allowPlayerSchematics.get())
                    {
                        // Don't have this pack on the server, disable.
                        StructurePacks.disablePack(pack.getName());
                    }
                }
                else if (version != pack.getVersion())
                {
                    // Version on the client is outdated. Set that we got pending changes.
                    StructurePacks.disablePack(pack.getName());
                    needsChanges = true;
                }
            }
        }

        for (final String packKey : serverStructurePacks.keySet())
        {
            if (!StructurePacks.hasPack(packKey))
            {
                needsChanges = true;
                break;
            }
        }

        if (!needsChanges)
        {
            // No new packs have be synced and no updated packs have to be synced.
            loadingState = ClientLoadingState.FINISHED_SYNCING;
            if (StructurePacks.selectedPack == null && !StructurePacks.getPackMetas().isEmpty())
            {
                StructurePacks.selectedPack = StructurePacks.getPackMetas().iterator().next();
            }
            StructurePacks.setFinishedLoading();
        }
    }

    /**
     * On reception of a new structure pack.
     *
     * @param packName the name of the structure pack.
     * @param payload the payload of the pack.
     * @param eol if last sync.
     */
    public static void onStructurePackTransfer(final String packName, final ByteBuf payload, final boolean eol)
    {
        Log.getLogger().warn("Received Structure pack from the Server: " + packName);
        IOPool.execute(() ->
        {
            final StructurePackMeta pack = StructurePacks.disablePack(packName);
            if (pack != null && !pack.isImmutable() && !JavaUtils.deleteDirectory(pack.getPath()))
            {
                Log.getLogger().warn("Error trying to delete pack: ");
            }

            try (ZipInputStream zis = new ZipInputStream(new ByteBufInputStream(payload)))
            {
                ZipEntry zipEntry = zis.getNextEntry();
                final Path structureFolder = Minecraft.getInstance().gameDirectory.toPath().resolve(BLUEPRINT_FOLDER);
                JavaUtils.deleteDirectory(structureFolder.resolve(packName));
                final Path rootPath = Files.createDirectory(structureFolder.resolve(packName));

                while (zipEntry != null)
                {
                    boolean isDirectory = zipEntry.isDirectory();
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
                StructurePacks.discoverPackAtPath(rootPath, true, modList, false, LOCAL);
            }
            catch (final IOException ex)
            {
                Log.getLogger().error("Unable to read datapack from zip", ex);
            }

            payload.release();
            if (eol)
            {
                loadingState = ClientLoadingState.FINISHED_SYNCING;
                StructurePacks.setFinishedLoading();
                StructurePacks.selectedPack = StructurePacks.getPackMetas().iterator().next();
            }
        });
    }

    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException
    {
        Path targetDirResolved = targetDir.resolve(zipEntry.getName());
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir.normalize()))
        {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }

    /**
     * Handles the save message of scans.
     *
     * @param compound compound to store.
     * @param fileName milli seconds for fileName.
     */
    public static void handleSaveScanMessage(final CompoundTag compound, final String fileName)
    {
        final String packName = Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US);
        StructurePacks.selectedPack = StructurePacks.getStructurePack(Minecraft.getInstance().getUser().getName());
        RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprintFuture(
          StructurePacks.storeBlueprint(packName, compound, Minecraft.getInstance().gameDirectory.toPath()
            .resolve(BLUEPRINT_FOLDER)
            .resolve(Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US))
            .resolve(SCANS_FOLDER).resolve(fileName)));
        RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setPos(null);
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("Scan successfully saved as %s", fileName), false);
    }
}
