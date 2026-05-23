package com.ldtteam.structurize.index;

import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.ldtteam.structurize.network.messages.SyncPackManagerMessage;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Static utility managing the global merged pack list across all loaded levels.
 * Each {@link ServerLevel} owns a {@link LevelPackData} that loads/saves its packs independently;
 * this class merges them into a single in-memory view and handles client synchronisation.
 */
public final class PackManager
{
    /**
     * Callback invoked on the client whenever the pack list is synchronised from the server.
     * Registered via {@link #addSyncListener}; held weakly so closed windows are collected automatically.
     */
    @FunctionalInterface
    public interface PackSyncListener
    {
        void onPackSync();
    }

    /**
     * Weakly-referenced sync listeners. Dead references are pruned on each sync.
     */
    private static final List<WeakReference<PackSyncListener>> syncListeners = new ArrayList<>();

    /**
     * Tracks which dimension each pack was loaded from, for cleanup on level unload.
     */
    private static final Map<String, ResourceKey<Level>> packOwnerDimension = new HashMap<>();

    /**
     * Live {@link LevelPackData} instances, one per currently loaded {@link ServerLevel}.
     */
    private static final Map<ResourceKey<Level>, LevelPackData> loadedLevelData = new HashMap<>();

    /**
     * Cached sorted view of the server-side merged pack map, invalidated whenever packs are added or levels load/unload.
     */
    private static List<Pack> serverPacksSorted = List.of();

    /**
     * Client-side pack list, populated via {@link SyncPackManagerMessage}.
     */
    private static final Map<String, Pack> clientPacks = new HashMap<>();

    /**
     * Cached sorted view of {@link #clientPacks}, invalidated whenever the map changes.
     */
    private static List<Pack> clientPacksSorted = List.of();

    private PackManager() {}

    /**
     * Loads each level's packs into the global map when the level loads.
     */
    public static void onLevelLoad(final LevelEvent.Load event)
    {
        if (!(event.getLevel() instanceof final ServerLevel serverLevel))
        {
            return;
        }

        final ResourceKey<Level> dimension = serverLevel.dimension();
        final LevelPackData data = serverLevel.getDataStorage().computeIfAbsent(LevelPackData.factoryFor(dimension), LevelPackData.DATA_NAME);

        loadedLevelData.put(dimension, data);

        for (final String packId : data.getOwnedPacks().keySet())
        {
            packOwnerDimension.put(packId, dimension);
        }

        invalidateServerPacksSorted();
    }

    /**
     * Removes the unloaded level's packs from the global map.
     */
    public static void onLevelUnload(final LevelEvent.Unload event)
    {
        if (!(event.getLevel() instanceof final ServerLevel serverLevel))
        {
            return;
        }

        final ResourceKey<Level> dimension = serverLevel.dimension();
        loadedLevelData.remove(dimension);
        packOwnerDimension.entrySet().removeIf(entry -> entry.getValue().equals(dimension));

        invalidateServerPacksSorted();

        if (loadedLevelData.isEmpty())
        {
            clientPacks.clear();
            clientPacksSorted = List.of();
        }
    }

    /**
     * Syncs the pack list to a player when they join.
     */
    public static void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!(event.getEntity() instanceof final ServerPlayer serverPlayer))
        {
            return;
        }

        PacketDistributor.sendToPlayer(serverPlayer, new SyncPackManagerMessage(getServerPacksMap()));
    }

    /**
     * Returns the server-sided pack list, sorted by name.
     */
    @NotNull
    public static List<Pack> getServerPacks()
    {
        return serverPacksSorted;
    }

    /**
     * Returns an unmodifiable merged view of all loaded levels' packs.
     * Used by {@link LevelPackData#setDirty} to populate sync messages.
     */
    @NotNull
    static Map<String, Pack> getServerPacksMap()
    {
        final Map<String, Pack> merged = new LinkedHashMap<>();
        for (final LevelPackData data : loadedLevelData.values())
        {
            merged.putAll(data.getOwnedPacks());
        }
        return Collections.unmodifiableMap(merged);
    }

    /**
     * Rebuilds the cached sorted server pack list from the current merged map.
     * Called whenever packs are added or levels load/unload.
     */
    static void invalidateServerPacksSorted()
    {
        serverPacksSorted = getServerPacksMap().values().stream().sorted(Comparator.comparing(Pack::name)).toList();
    }

    /**
     * Returns the client-sided pack list, sorted by name.
     */
    @NotNull
    public static List<Pack> getClientPacks()
    {
        return clientPacksSorted;
    }

    /**
     * Returns the client-side pack with the given ID, or {@code null} if not found.
     *
     * @param packId the pack identifier to look up
     * @return the pack, or {@code null}
     */
    @Nullable
    public static Pack getClientPack(final String packId)
    {
        return clientPacks.get(packId);
    }

    /**
     * Registers a listener that is notified whenever the client pack list is synchronised.
     * The listener is held via a {@link WeakReference}, so it does not need to be manually removed —
     * once the caller is garbage-collected the reference will be pruned on the next sync.
     */
    public static void addSyncListener(final @NotNull PackSyncListener listener)
    {
        syncListeners.add(new WeakReference<>(listener));
    }

    /**
     * Called by {@link SyncPackManagerMessage} on the client to store the synced pack list.
     */
    public static void onClientSync(final @NotNull Map<String, Pack> packs)
    {
        clientPacks.clear();
        clientPacks.putAll(packs);
        clientPacksSorted = clientPacks.values().stream().sorted(Comparator.comparing(Pack::name)).toList();

        final Iterator<WeakReference<PackSyncListener>> it = syncListeners.iterator();
        while (it.hasNext())
        {
            final PackSyncListener listener = it.next().get();
            if (listener == null)
            {
                it.remove();
            }
            else
            {
                listener.onPackSync();
            }
        }
    }

    /**
     * Adds a new pack, owned by the overworld's data file.
     *
     * @return the new pack's ID, or {@code null} if a pack with that name already exists or the overworld is not loaded.
     */
    @Nullable
    public static String addPack(final @NotNull String name, final @NotNull Holder<PackType> packType, final ServerLevel level)
    {
        final String id = name.toLowerCase(Locale.ROOT).replace(" ", "_");

        final LevelPackData target = loadedLevelData.get(level.dimension());
        if (target == null)
        {
            return null;
        }

        if (target.getOwnedPacks().containsKey(id))
        {
            return null;
        }

        final Pack pack = new Pack(id, name, packType);
        packOwnerDimension.put(id, level.dimension());
        target.getOwnedPacksMutable().put(id, pack);

        validatePack(pack.id(), level);

        target.setDirty();
        return id;
    }

    /**
     * Returns the pack with the given ID, or {@code null} if not loaded.
     */
    @Nullable
    public static Pack getPack(final @NotNull String packId)
    {
        final LevelPackData data = getLevelPackData(packId);
        return data != null ? data.getOwnedPacks().get(packId) : null;
    }

    /**
     * Runs pack-level validation on the given pack and syncs the result to all players.
     * Does not run per-schematic blueprint checks; use {@link #validateSchematic} for those.
     */
    public static void validatePack(final @NotNull String packId, final @NotNull ServerLevel level)
    {
        final LevelPackData data = getLevelPackData(packId);
        final Pack pack = getPack(packId);
        if (data == null || pack == null)
        {
            return;
        }

        PackValidator.validatePack(pack, level);
        data.setDirty();
    }

    /**
     * Runs per-schematic validation for schematics within the given pack that match the provided
     * path, name, and optionally level. After updating the affected schematics the pack is synced
     * to all players.
     *
     * <p>When {@code level} is {@code null} all schematics sharing the given path and name are
     * validated (every level of that schematic group). When a specific level is supplied only the
     * schematic entry for that level is validated.
     *
     * @param packId        the pack identifier
     * @param schematicPath the relative folder path of the schematic (may be empty for root-level)
     * @param schematicName the schematic file name (without extension)
     * @param level         the specific level to validate (1-based), or {@code null} for all levels
     * @param serverLevel   the server level used to resolve anchor block types
     */
    public static void validateSchematic(
        final @NotNull String packId,
        final @NotNull String schematicPath,
        final @NotNull String schematicName,
        final @Nullable Integer level,
        final @NotNull ServerLevel serverLevel)
    {
        final LevelPackData data = getLevelPackData(packId);
        final Pack pack = getPack(packId);
        if (data == null || pack == null)
        {
            return;
        }

        PackValidator.validateSchematic(pack, schematicPath, schematicName, level, serverLevel);
        data.setDirty();
    }

    /**
     * Adds or replaces a schematic in the given pack and marks that pack's owning level dirty.
     * If a schematic with the same path, name, and level already exists it is replaced.
     */
    public static void addSchematic(final @NotNull String packId, final @NotNull PackSchematic schematic)
    {
        final LevelPackData data = getLevelPackData(packId);
        if (data == null)
        {
            return;
        }

        final Pack pack = data.getOwnedPacks().get(packId);
        if (pack == null)
        {
            return;
        }

        data.getOwnedPacksMutable().put(packId, pack.withSchematic(schematic));
        data.setDirty();
    }

    @Nullable
    private static LevelPackData getLevelPackData(final @NotNull String packId)
    {
        final ResourceKey<Level> owner = packOwnerDimension.get(packId);
        return owner != null ? loadedLevelData.get(owner) : null;
    }
}
