package com.ldtteam.structurize.storage;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Class that contains all the structurepacks of the instance.
 */
public class StructurePacks
{
    /**
     * Current pack format.
     * Increase when the pack data format changes, or minecraft version changes require a full schematic update.
     * When the pack format doesn't align, the pack won't be loaded.
     */
    public static final int PACK_FORMAT = 1;

    /**
     * The list of registered structure packs.
     * This might be accessed concurrently by client/server. That's why it is a concurrent hashmap.
     */
    public static Map<String, StructurePackMeta> packMetas = new ConcurrentHashMap<>();

    // todo: get folder future (all sub folders of this folder)
    // todo: get display png path if folder has it.

    /**
     * Get a blueprint future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the path of the specific blueprint in the pack.
     * @return the blueprint future (might contain null).
     */
    public static Future<Blueprint> getBlueprintFuture(final String structurePackId, final String subPath)
    {
        return Util.ioPool().submit(() -> getBlueprint(structurePackId, subPath));
    }

    /**
     * Get a list blueprint future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the path of the specific blueprint in the pack.
     * @return the blueprint future (might contain null).
     */
    public static Future<List<Blueprint>> getBlueprintsFuture(final String structurePackId, final String subPath)
    {
        return Util.ioPool().submit(() -> getBlueprints(structurePackId, subPath));
    }

    /**
     * Get a blueprint directly (careful IO, might be slow).
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the folder containing the blueprints.
     * @return the blueprint or null.
     */
    @Nullable
    public static Blueprint getBlueprint(final String structurePackId, final String subPath)
    {
        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return null;
        }

        try
        {
            final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(packMeta.getPath().resolve(subPath))));
            return BlueprintUtil.readBlueprintFromNBT(nbt);
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprint: ", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a list of blueprints directly (careful IO, might be slow).
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the folder containing the blueprints.
     * @return the list of blueprints or empty
     */
    public static List<Blueprint> getBlueprints(final String structurePackId, final String subPath)
    {
        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return Collections.emptyList();
        }

        final List<Blueprint> blueprints = new ArrayList<>();

        try
        {
            Files.list(packMeta.getPath().resolve(subPath)).forEach(file -> {
                if (!Files.isDirectory(file) && file.endsWith("blueprint"))
                {
                    try
                    {
                        final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(file)));
                        blueprints.add(BlueprintUtil.readBlueprintFromNBT(nbt));
                    }
                    catch (final IOException e)
                    {
                        Log.getLogger().error("Error loading individual blueprint: " + file.toString(), e);
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprints from folder: " + subPath.toString(), e);
            e.printStackTrace();
        }

        return blueprints;
    }

    /**
     * Get a list of categories of a specific sub-path of a given structure pack.
     * This has IO, this may be slow.
     * @param structurePackId the id of the pack.
     * @param subPath the sub-path.
     * @return the list of categories.
     */
    public static List<Category> getCategories(final String structurePackId, final String subPath)
    {
        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return Collections.emptyList();
        }

        final List<Category> categories = new ArrayList<>();

        try
        {
            Files.list(packMeta.getPath().resolve(subPath)).forEach(file -> {

                //we're in this folder, and we want to check if there are sub-folders in here. For each of the usbfolders we create a category,
                // each of the subfolders we scan to check if they got an icon and if they got further sub-folders. Then we create a category for them.

                if (!Files.isDirectory(file) && file.endsWith("blueprint"))
                {
                    try
                    {
                        final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(file)));
                        blueprints.add(BlueprintUtil.readBlueprintFromNBT(nbt));
                    }
                    catch (final IOException e)
                    {
                        Log.getLogger().error("Error loading individual blueprint: " + file.toString(), e);
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprints from folder: " + subPath.toString(), e);
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Discover a structure pack at a given path.
     * @param element the path to check for.
     * @param immutable if jar (true), else false.
     * @param modList the list of mods loaded on this instance.
     */
    public static void discoverPackAtPath(final Path element, final boolean immutable, final List<String> modList)
    {
        final Path packJsonPath = element.resolve("pack.json");
        if (Files.exists(packJsonPath))
        {
            try (final JsonReader reader = new JsonReader(Files.newBufferedReader(packJsonPath)))
            {
                final StructurePackMeta pack = new StructurePackMeta(Streams.parse(reader).getAsJsonObject(), element);
                if (pack.getPackFormat() == PACK_FORMAT)
                {
                    pack.setImmutable(immutable);
                    for (final String modId : pack.getModList())
                    {
                        if (!modList.contains(modId))
                        {
                            Log.getLogger().warn("Missing Mod: " + modId + " for Pack: " + pack.getName());
                            return;
                        }
                    }
                    packMetas.put(pack.getName(), pack);
                    Log.getLogger().info("Registered structure pack: " + pack.getName());
                }
                else
                {
                    Log.getLogger().warn("Wrong Pack Format: " + pack.getName());
                }
            }
            catch (final IOException ex)
            {
                Log.getLogger().warn("Error Reading pack: ", ex);
            }
        }
    }

    /**
     * The representation of a structure pack category (a folder).
     */
    public static class Category
    {
        /**
         * The pack meta the category belongs to.
         */
        public StructurePackMeta packMeta;

        /**
         * The sub-path of the category.
         */
        public String subPath;

        /**
         * If the category has its own icon.
         */
        public boolean hasIcon;

        /**
         * If the category got further sub-categories.
         */
        public boolean isTerminal;
    }
}
