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

import java.util.*;

/**
 * Static utility managing the global merged pack list across all loaded levels.
 * Each {@link ServerLevel} owns a {@link LevelPackData} that loads/saves its packs independently;
 * this class merges them into a single in-memory view and handles client synchronisation.
 */
public final class PackManager
{
    /**
     * Tracks which dimension each pack was loaded from, for cleanup on level unload.
     */
    private static final Map<String, ResourceKey<Level>> packOwnerDimension = new HashMap<>();

    /**
     * Live {@link LevelPackData} instances, one per currently loaded {@link ServerLevel}.
     */
    private static final Map<ResourceKey<Level>, LevelPackData> loadedLevelData = new HashMap<>();

    /**
     * Client-side pack list, populated via {@link SyncPackManagerMessage}.
     */
    private static Map<String, Pack> clientPacks = new HashMap<>();

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

        if (loadedLevelData.isEmpty())
        {
            clientPacks.clear();
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
        return getServerPacksMap().values().stream().sorted(Comparator.comparing(Pack::name)).toList();
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
     * Returns the client-sided pack list, sorted by name.
     */
    @NotNull
    public static List<Pack> getClientPacks()
    {
        return clientPacks.values().stream().sorted(Comparator.comparing(Pack::name)).toList();
    }

    /**
     * Called by {@link SyncPackManagerMessage} on the client to store the synced pack list.
     */
    public static void onClientSync(final @NotNull Map<String, Pack> packs)
    {
        clientPacks = new HashMap<>(packs);
    }

    /**
     * Adds a new pack, owned by the overworld's data file.
     *
     * @return the new pack's ID, or {@code null} if a pack with that name already exists or the overworld is not loaded.
     */
    @Nullable
    public static String addPack(final @NotNull String name, final @NotNull Holder<PackType> packType)
    {
        final String id = name.toLowerCase(Locale.ROOT).replace(" ", "_");

        final LevelPackData target = loadedLevelData.get(Level.OVERWORLD);
        if (target == null)
        {
            return null;
        }

        if (target.getOwnedPacks().containsKey(id))
        {
            return null;
        }

        final Pack pack = new Pack(id, name, packType);
        packOwnerDimension.put(id, Level.OVERWORLD);
        target.getOwnedPacksMutable().put(id, pack);
        target.setDirty();
        return id;
    }

    /**
     * Returns the pack with the given ID, or {@code null} if not loaded.
     */
    @Nullable
    public static Pack getPack(final @NotNull String packId)
    {
        final ResourceKey<Level> owner = packOwnerDimension.get(packId);
        final LevelPackData data = owner != null ? loadedLevelData.get(owner) : null;
        return data != null ? data.getOwnedPacks().get(packId) : null;
    }

    /**
     * Adds or replaces a schematic in the given pack and marks that pack's owning level dirty.
     * If a schematic with the same path, name, and level already exists it is replaced.
     */
    public static void addSchematic(final @NotNull String packId, final @NotNull PackSchematic schematic)
    {
        final ResourceKey<Level> owner = packOwnerDimension.get(packId);
        final LevelPackData data = owner != null ? loadedLevelData.get(owner) : null;
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
}
