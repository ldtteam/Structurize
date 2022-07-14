package com.ldtteam.structurize.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.network.messages.NotifyServerAboutStructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.util.constant.Constants.SCANS_FOLDER;

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
            modPaths.add(mod.getOwningFile().getFile().findResource(BLUEPRINT_FOLDER, mod.getModId()));
            modList.add(mod.getModId());
        }

        final Path gameFolder = Minecraft.getInstance().gameDirectory.toPath();

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
        if (serverStructurePacks.isEmpty())
        {
            // Most likely single player. Skip.
            loadingState = ClientLoadingState.FINISHED_SYNCING;
            StructurePacks.finishedLoading = true;
            if (StructurePacks.selectedPack == null && !StructurePacks.packMetas.isEmpty())
            {
                StructurePacks.selectedPack = StructurePacks.packMetas.values().iterator().next();
            }
            return;
        }

        boolean needsChanges = false;
        for (final Map.Entry<String, StructurePackMeta> entry : new ArrayList<>(StructurePacks.packMetas.entrySet()))
        {
            if (!entry.getValue().isImmutable())
            {
                final int version = serverStructurePacks.getOrDefault(entry.getKey(), -1);
                if (version == -1 && !Structurize.getConfig().getServer().allowPlayerSchematics.get())
                {
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
            if (StructurePacks.selectedPack == null && !StructurePacks.packMetas.isEmpty())
            {
                StructurePacks.selectedPack = StructurePacks.packMetas.values().iterator().next();
            }
            StructurePacks.finishedLoading = true;
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
        Util.ioPool().execute(() ->
        {
            final StructurePackMeta pack = StructurePacks.packMetas.remove(packName);
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
                final Path structureFolder = Minecraft.getInstance().gameDirectory.toPath().resolve(BLUEPRINT_FOLDER);
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
            if (eol)
            {
                loadingState = ClientLoadingState.FINISHED_SYNCING;
                StructurePacks.finishedLoading = true;
            }
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

    /**
     * Handles the save message of scans.
     *
     * @param compound compound to store.
     * @param fileName milli seconds for fileName.
     */
    public static void handleSaveScanMessage(final CompoundTag compound, final String fileName)
    {
        final String packName = Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US);
        RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprintFuture(
          StructurePacks.storeBlueprint(packName, compound, Minecraft.getInstance().gameDirectory.toPath()
            .resolve(BLUEPRINT_FOLDER)
            .resolve(Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US))
            .resolve(SCANS_FOLDER).resolve(fileName)));
        Minecraft.getInstance().player.sendMessage(new TranslatableComponent("Scan successfully saved as %s", fileName), Minecraft.getInstance().player.getUUID());
    }
}