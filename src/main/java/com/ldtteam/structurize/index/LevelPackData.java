package com.ldtteam.structurize.index;

import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.network.messages.SyncPackManagerMessage;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Per-level SavedData storing the packs that belong to a specific level's data folder.
 * One instance is loaded per {@link net.minecraft.server.level.ServerLevel}; the merged view is managed by {@link PackManager}.
 */
public class LevelPackData extends SavedData
{
    public static final String DATA_NAME = "structurize_pack_manager";

    private static final String NBT_PACKS = "packs";

    private final ResourceKey<Level> dimension;

    private final Map<String, Pack> ownedPacks = new HashMap<>();

    private LevelPackData(final ResourceKey<Level> dimension)
    {
        this.dimension = dimension;
    }

    /**
     * Returns a factory for the given dimension. Must be constructed per call-site since the
     * dimension key is needed for the empty-constructor supplier.
     */
    public static SavedData.Factory<LevelPackData> factoryFor(final ResourceKey<Level> dimension)
    {
        return new SavedData.Factory<>(() -> new LevelPackData(dimension), (tag, provider) -> deserialize(tag, provider, dimension));
    }

    private static LevelPackData deserialize(final @NotNull CompoundTag compound, final @NotNull HolderLookup.Provider provider, final ResourceKey<Level> dimension)
    {
        final LevelPackData data = new LevelPackData(dimension);
        final ListTag list = compound.getList(NBT_PACKS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final Pack pack = Pack.load(list.getCompound(i), provider);
            data.ownedPacks.put(pack.id(), pack);
        }
        return data;
    }

    @Override
    @NotNull
    public CompoundTag save(final @NotNull CompoundTag tag, final @NotNull HolderLookup.Provider provider)
    {
        final ListTag list = new ListTag();
        for (final Pack pack : ownedPacks.values())
        {
            list.add(pack.save(provider));
        }
        tag.put(NBT_PACKS, list);
        return tag;
    }

    @Override
    public void setDirty(final boolean value)
    {
        super.setDirty(value);
        if (value)
        {
            PacketDistributor.sendToAllPlayers(new SyncPackManagerMessage(PackManager.getServerPacksMap()));
        }
    }

    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    /**
     * Read-only view of packs owned by this level, used during load merging.
     */
    public Map<String, Pack> getOwnedPacks()
    {
        return ownedPacks;
    }

    /**
     * Mutable access for PackManager to insert newly created packs into this level's owned set.
     * Package-private intentionally.
     */
    Map<String, Pack> getOwnedPacksMutable()
    {
        return ownedPacks;
    }
}
