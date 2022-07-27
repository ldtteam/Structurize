package com.ldtteam.structurize.storage;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.util.IOPool;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Predicate;

/**
 * Class that contains all the structurepacks of the instance.
 */
public class StructurePacks
{
    /*
     * todo add md5 support in the future (stronger consistency guarantee).
     * This way we can be 100% sure that the pack has the correct version.
     */

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

    /**
     * Set to true on client/server once style loading has finished.
     */
    public static volatile boolean finishedLoading = false;

    /**
     * Selected pack on the client.
     */
    public static StructurePackMeta selectedPack;

    /**
     * Get a blueprint future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the path of the specific blueprint in the pack.
     * @return the blueprint future (might contain null).
     */
    public static Future<Blueprint> getBlueprintFuture(final String structurePackId, final String subPath)
    {
        return IOPool.submit(() -> getBlueprint(structurePackId, subPath));
    }

    /**
     * Get a blueprint data future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the path of the specific blueprint in the pack.
     * @return the blueprint data future (might contain null).
     */
    public static Future<byte[]> getBlueprintDataFuture(final String structurePackId, final String subPath)
    {
        return IOPool.submit(() -> getBlueprintData(structurePackId, subPath));
    }

    /**
     * Find a blueprint future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param name the filename.
     * @return the blueprint future (might contain null).
     */
    public static Future<Path> findBlueprintFuture(final String structurePackId, final String name)
    {
        return IOPool.submit(() -> findBlueprint(structurePackId, name));
    }

    /**
     * Get a list blueprint future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the path of the set of blueprints (usually a folder).
     * @return the blueprints list (might be empty).
     */
    public static Future<List<Blueprint>> getBlueprintsFuture(final String structurePackId, final String subPath)
    {
        return IOPool.submit(() -> getBlueprints(structurePackId, subPath));
    }

    /**
     * Get a list of categories of a specific sub-path of a given structure pack.
     * @param structurePackId the id of the pack.
     * @param subPath the sub-path.
     * @return the list of categories.
     */
    public static Future<List<Category>> getCategoriesFuture(final String structurePackId, final String subPath)
    {
        return IOPool.submit(() -> getCategories(structurePackId, subPath));
    }

    /**
     * Get the blueprint directly with a path.
     * @param packName the pack we're looking in.
     * @param path the path to search for.
     * @return the blueprint.
     */
    public static Future<Blueprint> getBlueprintFuture(final String packName, final Path path)
    {
        return IOPool.submit(() -> getBlueprint(packName, path));
    }

    /**
     * Find the blueprint async
     * @param blueprintPredicate the predicate to define the blueprint we're looking for.
     * @return the blueprint future.
     */
    public static Future<Blueprint> findBlueprintFuture(final String structurePackId, final Predicate<Blueprint> blueprintPredicate)
    {
        return IOPool.submit(() -> findBlueprint(structurePackId, blueprintPredicate));
    }

    /**
     * Find a blueprint by name.
     * @param structurePackId the pack to search in.
     * @param name the name we're searching for.
     * @return the path or null.
     */
    public static Path findBlueprint(final String structurePackId, final String name)
    {
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return null;
        }

        return findBlueprint(packMeta.getPath(), name).orElse(null);
    }

    /**
     * Find blueprint at path.
     * Recursively goes through folder structure.
     * @param subPath the sub path to check.
     * @param name the name of the file we're looking for.
     * @return the path of the file or null.
     */
    public static Optional<Path> findBlueprint(final Path subPath, final String name)
    {
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        try
        {
            return Files.walk(subPath).filter(file -> {
                if (!Files.isDirectory(file) && file.toString().endsWith("blueprint"))
                {
                    return file.getFileName().toString().replace(".blueprint", "").equals(name);
                }
                return false;
            }).findFirst();
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprint: ", e);
        }
        return Optional.empty();
    }

    /**
     * Find a blueprint by name.
     * @param structurePackId the pack to search in.
     * @param blueprintPredicate matches the blueprint.
     * @return the blueprint or null.
     */
    public static Blueprint findBlueprint(final String structurePackId, final Predicate<Blueprint> blueprintPredicate)
    {
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return null;
        }

        return findBlueprint(packMeta.getName(), packMeta.getPath(), blueprintPredicate);
    }

    /**
     * Find blueprint at path.
     * Recursively goes through folder structure.
     * @param pack the pack we're looking in.
     * @param subPath the sub path to check.
     * @param blueprintPredicate matches the blueprint.
     * @return the path of the file or null.
     */
    public static Blueprint findBlueprint(final String pack, final Path subPath, final Predicate<Blueprint> blueprintPredicate)
    {
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        try
        {
            return Files.list(subPath).map(file -> {
                if (!Files.isDirectory(file) && file.toString().endsWith("blueprint"))
                {
                    final Blueprint blueprint = getBlueprint(pack, file);
                    if (blueprintPredicate.test(blueprint))
                    {
                        blueprint.setFileName(file.getFileName().toString().replace(".blueprint", ""));
                        blueprint.setFilePath(file.getParent()).setPackName(pack);
                        return blueprint;
                    }
                }
                else if (Files.isDirectory(file))
                {
                    return findBlueprint(pack, file, blueprintPredicate);
                }
                return  null;
            }).filter(Objects::nonNull).findFirst().orElse(null);
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprint: ", e);
        }
        return null;
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
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return null;
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        return getBlueprint(structurePackId, packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath)));
    }

    /**
     * Get the blueprint directly with a path.
     * @param pack the pack this belongs to.
     * @param path the path to search for.
     * @return the blueprint.
     */
    public static Blueprint getBlueprint(final String pack, final Path path)
    {
        try
        {
            final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(path)));
            final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(nbt);

            blueprint.setFileName(path.getFileName().toString().replace(".blueprint", ""));
            blueprint.setFilePath(path.getParent()).setPackName(pack);

            return blueprint;
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprint: ", e);
        }
        return null;
    }

    /**
     * Get blueprint data directly (careful IO, might be slow).
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the folder containing the blueprints.
     * @return the blueprint data in a byte array or null.
     */
    public static byte[] getBlueprintData(final String structurePackId, final String subPath)
    {
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return null;
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        try
        {
            return Files.readAllBytes(packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath)));
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error reading blueprint data: ", e);
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
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return Collections.emptyList();
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        final List<Blueprint> blueprints = new ArrayList<>();

        try
        {
            Files.list(packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath))).forEach(file -> {
                if (!Files.isDirectory(file) && file.toString().endsWith("blueprint"))
                {
                    try
                    {
                        final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(file)));
                        final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(nbt);
                        blueprint.setFileName(file.getFileName().toString().replace(".blueprint", ""));
                        blueprint.setFilePath(file.getParent()).setPackName(structurePackId);
                        blueprints.add(blueprint);
                    }
                    catch (final IOException e)
                    {
                        Log.getLogger().error("Error loading individual blueprint: " + file, e);
                    }
                }
            });
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprints from folder: " + packMeta.getNormalizedSubPath(subPath), e);
        }

        blueprints.sort(Comparator.comparing(Blueprint::getFileName));
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
        while (!finishedLoading)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                // Nothing on purpose.
            }
        }

        final StructurePackMeta packMeta = packMetas.get(structurePackId);
        if (packMeta == null)
        {
            return Collections.emptyList();
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        final List<Category> categories = new ArrayList<>();

        try
        {
            Files.list(packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath))).forEach(file -> {

                if (Files.isDirectory(file))
                {
                    final Category newCategory = new Category(packMeta, file, false, true);
                    newCategory.hasIcon = false;
                    newCategory.isTerminal = true;

                    try
                    {
                        Files.list(file).forEach(subFile -> {
                            if (subFile.endsWith("icon.png"))
                            {
                                newCategory.hasIcon = true;
                            }
                            else if (Files.isDirectory(subFile))
                            {
                                newCategory.isTerminal = false;
                            }
                        });
                        categories.add(newCategory);
                    }
                    catch (final IOException e)
                    {
                        Log.getLogger().error("Error loading category: " + file, e);
                    }
                }
            });
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading categories from folder: " + packMeta.getNormalizedSubPath(subPath), e);
        }
        return categories;
    }

    /**WindowExtendedBuildTool
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
     * Store a blueprint at a given path.
     * @param packName the pack we're storing it in.
     * @param compoundTag compound to store.
     * @param path path to store it at.
     */
    public static Future<Blueprint> storeBlueprint(final String packName, final CompoundTag compoundTag, final Path path)
    {
        return IOPool.submit(() ->
        {
            Files.createDirectories(path.getParent());
            try (final OutputStream outputstream = new BufferedOutputStream(Files.newOutputStream(path)))
            {
                NbtIo.writeCompressed(compoundTag, outputstream);
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("Exception while trying to scan.", e);
                return null;
            }
            return StructurePacks.getBlueprint(packName, path);
        });
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

        /**
         * Create an empty category.
         */
        public Category()
        {
            // Intentionally left empty.
        }

        /**
         * Create a new category.
         * @param packMeta the structure pack it belongs to.
         * @param subPath the sub path.
         * @param hasIcon if it has an icon.
         * @param isTerminal if it's terminal (no further sub-folders).
         */
        public Category(final StructurePackMeta packMeta, final Path subPath, final boolean hasIcon, final boolean isTerminal)
        {
            this.packMeta = packMeta;
            this.subPath = packMeta.getSubPath(subPath).replace("\\", "/");
            this.hasIcon = hasIcon;
            this.isTerminal = isTerminal;
        }
    }
}
