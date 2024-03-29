package com.ldtteam.structurize.storage;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.util.IOPool;
import com.ldtteam.structurize.util.ManualBarrier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    private static final int PACK_FORMAT = 1;

    /**
     * The list of registered structure packs.
     * This might be accessed concurrently by client/server. That's why it is a concurrent hashmap.
     */
    private static final Map<String, StructurePackMeta> packMetas = new ConcurrentHashMap<>();

    /**
     * The list of registered client structure packs.
     * This is always ever only accessed from the server side.
     * Potentially from multiple threads.
     * This is not synced to the clients.
     */
    private static final Map<String, StructurePackMeta> clientPackMetas = new ConcurrentHashMap<>();

    /**
     * Set to true on client/server once style loading has finished.
     */
    private static final ManualBarrier finishedLoading = new ManualBarrier(false);

    /**
     * Selected pack on the client.
     */
    public static StructurePackMeta selectedPack;

    /**
     * Blocks the current thread until loading has finished
     * @return true if finished; false if interrupted before finishing
     */
    public static boolean waitUntilFinishedLoading()
    {
        try
        {
            finishedLoading.waitOne();
            return true;
        }
        catch (InterruptedException e)
        {
            return false;
        }
    }

    /**
     * Conclude loading.
     */
    public static void setFinishedLoading()
    {
        finishedLoading.open();
    }

    /**
     * Get the list of pack meta.
     * @return the pack meta set.
     */
    public static Collection<StructurePackMeta> getPackMetas()
    {
        return packMetas.values();
    }

    /**
     * Query a structure pack for a key.
     * @param key the used key.
     * @return the pack or null.
     */
    @Nullable
    public static StructurePackMeta getStructurePack(final String key)
    {
        if (packMetas.containsKey(key))
        {
            return packMetas.get(key);
        }

        return clientPackMetas.get(key);
    }

    /**
     * Reset pack state.
     */
    public static void clearPacks()
    {
        packMetas.clear();
        clientPackMetas.clear();
    }

    /**
     * Disable a specific pack.
     * @param name the packname.
     */
    public static StructurePackMeta disablePack(final String name)
    {
        return packMetas.remove(name);
    }

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
     * Get a blueprint future.
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the path of the specific blueprint in the pack.
     * @param suppressError log exception or not.
     * @return the blueprint future (might contain null).
     */
    public static Future<Blueprint> getBlueprintFuture(final String structurePackId, final String subPath, final boolean suppressError)
    {
        return IOPool.submit(() -> getBlueprint(structurePackId, subPath, suppressError));
    }


    /**
     * Get the blueprint directly with a path.
     * @param packName the pack we're looking in.
     * @param path the path to search for.
     * @param suppressError log exception or not.
     * @return the blueprint.
     */
    public static Future<Blueprint> getBlueprintFuture(final String packName, final Path path, final boolean suppressError)
    {
        return IOPool.submit(() -> getBlueprint(packName, path, suppressError));
    }

    // ------------------------- Synchronous Calls ------------------------- //

    /**
     * Get a blueprint directly (careful IO, might be slow).
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the folder containing the blueprints.
     * @return the blueprint or null.
     */
    @Nullable
    public static Blueprint getBlueprint(final String structurePackId, final String subPath)
    {
        return getBlueprint(structurePackId, subPath, false);
    }

    /**
     * Get the blueprint directly with a path.
     * @param pack the pack this belongs to.
     * @param path the path to search for.
     * @return the blueprint.
     */
    public static Blueprint getBlueprint(final String pack, final Path path)
    {
        return getBlueprint(pack, path, false);
    }

    /**
     * Find a blueprint by name.
     * @param structurePackId the pack to search in.
     * @param name the name we're searching for.
     * @return the path or null.
     */
    public static Path findBlueprint(final String structurePackId, final String name)
    {
        if (!waitUntilFinishedLoading())
        {
            return null;
        }

        final StructurePackMeta packMeta = getStructurePack(structurePackId);
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
        if (!waitUntilFinishedLoading())
        {
            return Optional.empty();
        }

        try
        {
            try (final Stream<Path> paths = Files.walk(subPath))
            {
                return paths.filter(file ->
                {
                    if (!Files.isDirectory(file) && file.toString().endsWith("blueprint"))
                    {
                        return file.getFileName().toString().replace(".blueprint", "").equals(name);
                    }
                    return false;
                }).findFirst();
            }
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprint: " + subPath + ":" + name, e);
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
        if (!waitUntilFinishedLoading())
        {
            return null;
        }

        final StructurePackMeta packMeta = getStructurePack(structurePackId);
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
        if (!waitUntilFinishedLoading())
        {
            return null;
        }

        try
        {
            try (final Stream<Path> paths = Files.list(subPath))
            {
                return paths.map(file -> {
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
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading blueprint: " + pack + ":" + subPath, e);
        }
        return null;
    }

    /**
     * Get a blueprint directly (careful IO, might be slow).
     * @param structurePackId the structure pack the blueprint is in.
     * @param subPath the folder containing the blueprints.
     * @param suppressError log exception or not.
     * @return the blueprint or null.
     */
    @Nullable
    public static Blueprint getBlueprint(final String structurePackId, final String subPath, final boolean suppressError)
    {
        if (!waitUntilFinishedLoading())
        {
            return null;
        }

        final StructurePackMeta packMeta = getStructurePack(structurePackId);
        if (packMeta == null)
        {
            return null;
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        return getBlueprint(structurePackId, packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath)), suppressError);
    }

    /**
     * Get the blueprint directly with a path.
     * @param pack the pack this belongs to.
     * @param path the path to search for.
     * @param suppressError log exception or not.
     * @return the blueprint.
     */
    public static Blueprint getBlueprint(final String pack, final Path path, final boolean suppressError)
    {
        try
        {
            final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(path)));
            final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(nbt);
            if (blueprint == null) return null;

            blueprint.setFileName(path.getFileName().toString().replace(".blueprint", ""));
            blueprint.setFilePath(path.getParent()).setPackName(pack);

            return blueprint;
        }
        catch (final IOException e)
        {
            if (!suppressError)
            {
                Log.getLogger().error("Error loading blueprint: "  + pack + ":" + path, e);
            }
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
        if (!waitUntilFinishedLoading())
        {
            return null;
        }

        final StructurePackMeta packMeta = getStructurePack(structurePackId);
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
        if (!waitUntilFinishedLoading())
        {
            return Collections.emptyList();
        }

        final StructurePackMeta packMeta = getStructurePack(structurePackId);
        if (packMeta == null)
        {
            return Collections.emptyList();
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        final List<Blueprint> blueprints = new ArrayList<>();

        try
        {
            try (final Stream<Path> paths = Files.list(packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath))))
            {
                paths.forEach(file -> {
                    if (!Files.isDirectory(file) && file.toString().endsWith("blueprint"))
                    {
                        try
                        {
                            final CompoundTag nbt = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(file)));
                            final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(nbt);
                            if (blueprint != null)
                            {
                                blueprint.setFileName(file.getFileName().toString().replace(".blueprint", ""));
                                blueprint.setFilePath(file.getParent()).setPackName(structurePackId);
                                blueprints.add(blueprint);
                            }
                        }
                        catch (final IOException e)
                        {
                            Log.getLogger().error("Error loading individual blueprint: " + file, e);
                        }
                    }
                });
            }
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
        if (!waitUntilFinishedLoading())
        {
            return Collections.emptyList();
        }

        final StructurePackMeta packMeta = getStructurePack(structurePackId);
        if (packMeta == null)
        {
            return Collections.emptyList();
        }

        //todo, here similarly as in the other places we could query a remote server for this if we don't have it locally.

        final Path basePath = packMeta.getPath().resolve(packMeta.getNormalizedSubPath(subPath));
        final List<Category> categories = new ArrayList<>();
        boolean hasBlueprints = false;

        try
        {
            try (final Stream<Path> paths = Files.list(basePath))
            {
                for (final Path file : paths.toList())
                {
                    if (Files.isDirectory(file))
                    {
                        final Category newCategory = new Category(packMeta, file, false, true, false);

                        try
                        {
                            try (final Stream<Path> subPaths = Files.list(file))
                            {
                                subPaths.forEach(subFile -> {
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
                        }
                        catch (final IOException e)
                        {
                            Log.getLogger().error("Error loading category: " + file, e);
                        }
                    }
                    else if (file.toString().endsWith(".blueprint"))
                    {
                        hasBlueprints = true;
                    }
                }
            }
        }
        catch (final IOException e)
        {
            Log.getLogger().error("Error loading categories from folder: " + packMeta.getNormalizedSubPath(subPath), e);
        }

        if (hasBlueprints && !categories.isEmpty())
        {
            // also add the current path as a terminal path when it directly contains both blueprints and folders.
            categories.add(new Category(packMeta, basePath, false, true, true));
        }

        categories.sort(Comparator.comparing(Category::toString));
        return categories;
    }

    /**
     * Discover a structure pack at a given path.
     * @param element the path to check for.
     * @param immutable if jar (true), else false.
     * @param modList the list of mods loaded on this instance.
     * @param clientPack if this is a client pac.
     */
    /*
    public static void discoverPackAtPath(final Path element, final boolean immutable, final List<String> modList, final boolean clientPack)
    {
        discoverPackAtPath(element, immutable, modList, clientPack, "unknown");
    }*/

    /**
     * Discover a structure pack at a given path.
     * @param element the path to check for.
     * @param immutable if jar (true), else false.
     * @param modList the list of mods loaded on this instance.
     * @param clientPack if this is a client pac.
     */
    public static void discoverPackAtPath(final Path element, final boolean immutable, final List<String> modList, final boolean clientPack, final String owner)
    {
        final Path packJsonPath = element.resolve("pack.json");
        if (Files.exists(packJsonPath))
        {
            try
            {
                try (final JsonReader reader = new JsonReader(Files.newBufferedReader(packJsonPath));)
                {
                    final StructurePackMeta pack = new StructurePackMeta(Streams.parse(reader).getAsJsonObject(), element, owner);
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
                        if (clientPack)
                        {
                            clientPackMetas.put(pack.getName(), pack);
                        }
                        else
                        {
                            packMetas.put(pack.getName(), pack);
                        }
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
            catch (final Exception ex)
            {
                Log.getLogger().warn("Error Reading Json: " + element, ex);
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
     * Check if the pack exists.
     * @param key the pack id to check.
     * @return true if so.
     */
    public static boolean hasPack(final String key)
    {
        return packMetas.containsKey(key);
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
         * If the category does not have further sub-categories.
         */
        public boolean isTerminal;

        /**
         * If this is a special "current folder" terminal category.
         */
        public boolean isCurrent;

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
         * @param isCurrent if it's a "current directory" subcategory.
         */
        public Category(final StructurePackMeta packMeta, final Path subPath, final boolean hasIcon, final boolean isTerminal, final boolean isCurrent)
        {
            this.packMeta = packMeta;
            this.subPath = packMeta.getSubPath(subPath).replace("\\", "/");
            this.hasIcon = hasIcon;
            this.isTerminal = isTerminal;
            this.isCurrent = isCurrent;

            if (this.subPath.endsWith("/"))
            {
                this.subPath = this.subPath.substring(0, this.subPath.length() - 1);
            }
            if (this.subPath.startsWith("/"))
            {
                this.subPath = this.subPath.substring(1);
            }
            if (this.isCurrent)
            {
                this.subPath = this.subPath + "/.";
            }
        }

        @Override
        public String toString()
        {
            return subPath;
        }
    }
}
