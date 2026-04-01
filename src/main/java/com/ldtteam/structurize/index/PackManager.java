package com.ldtteam.structurize.index;

import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.ldtteam.structurize.network.messages.SyncPackManagerMessage;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Global SavedData stored in the overworld, holding all packs across the game.
 * Synced to clients on player join.
 */
public class PackManager extends SavedData
{
    public static final String DATA_NAME = "structurize_pack_manager";

    public static final SavedData.Factory<PackManager> FACTORY = new SavedData.Factory<>(PackManager::new, PackManager::deserialize);

    /**
     * Client-side pack list, populated via {@link SyncPackManagerMessage}.
     */
    private static Map<String, Pack> clientPacks = new HashMap<>();

    private static final String NBT_PACKS = "packs";

    private final Map<String, Pack> packs = new HashMap<>();

    private PackManager() {}

    private static PackManager deserialize(final @NotNull CompoundTag compound, final @NotNull HolderLookup.Provider provider)
    {
        final PackManager manager = new PackManager();
        final ListTag list = compound.getList(NBT_PACKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final Pack pack = Pack.load(list.getCompound(i), provider);
            manager.packs.put(pack.getId(), pack);
        }
        return manager;
    }

    @Override
    @NotNull
    public CompoundTag save(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider provider)
    {
        final ListTag list = new ListTag();
        for (final Pack pack : packs.values())
        {
            list.add(pack.save(provider));
        }
        tag.put(NBT_PACKS, list);
        return tag;
    }

    /**
     * Loads the PackManager from the overworld data storage.
     * Call on overworld load only.
     */
    public static void onLevelLoad(final LevelEvent.Load event)
    {
        if (!(event.getLevel() instanceof final ServerLevel serverLevel))
        {
            return;
        }

        if (serverLevel.dimension() != Level.OVERWORLD)
        {
            return;
        }

        serverLevel.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    /**
     * Clears client-side pack list on overworld unload.
     */
    public static void onLevelUnload(final LevelEvent.Unload event)
    {
        if (!(event.getLevel() instanceof final ServerLevel serverLevel))
        {
            return;
        }

        if (serverLevel.dimension() != Level.OVERWORLD)
        {
            return;
        }

        clientPacks.clear();
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

        final ServerLevel overworld = serverPlayer.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null)
        {
            return;
        }

        final PackManager manager = overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        PacketDistributor.sendToPlayer(serverPlayer, new SyncPackManagerMessage(manager.packs));
    }

    /**
     * Returns the server-sided pack list from the overworld data storage.
     */
    @NotNull
    public static List<Pack> getServerPacks(final @NotNull ServerLevel overworld)
    {
        final Map<String, Pack> packs = overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME).packs;
        return packs.values().stream().sorted(Comparator.comparing(Pack::getName)).toList();
    }

    /**
     * Returns the client-sided pack list from the overworld data storage.
     */
    @NotNull
    public static List<Pack> getClientPacks()
    {
        return clientPacks.values().stream().sorted(Comparator.comparing(Pack::getName)).toList();
    }

    /**
     * Called by {@link SyncPackManagerMessage} on the client to store the synced pack list.
     */
    public static void onClientSync(final @NotNull Map<String, Pack> packs)
    {
        clientPacks = new HashMap<>(packs);
    }

    @Override
    public void setDirty(boolean value)
    {
        super.setDirty(value);
        if (value)
        {
            PacketDistributor.sendToAllPlayers(new SyncPackManagerMessage(packs));
        }
    }

    @Nullable
    public String addPack(final @NotNull String name, final @NotNull Holder<PackType> packType)
    {
        final String id = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        if (packs.containsKey(id))
        {
            return null;
        }
        packs.put(id, new Pack(id, name, packType));
        setDirty();
        return id;
    }

    public void addSchematic(final @NotNull String packId, final @NotNull PackSchematic schematic)
    {
        final Pack pack = packs.get(packId);
        if (pack != null)
        {
            pack.addSchematic(schematic);
            setDirty();
        }
    }
}
