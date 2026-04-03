package com.ldtteam.structurize.index.models;

import com.ldtteam.structurize.api.Registries;
import com.ldtteam.structurize.index.packtypes.models.PackType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record Pack(
    @NotNull String id,
    @NotNull String name,
    @NotNull Holder<PackType> type,
    @NotNull Set<PackSchematic> schematics)
{
    private static final Codec<Set<PackSchematic>> SCHEMATICS_CODEC = PackSchematic.CODEC.listOf().xmap(HashSet::new, ArrayList::new);

    public static final Codec<Pack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(Pack::id),
        Codec.STRING.fieldOf("name").forGetter(Pack::name),
        RegistryFixedCodec.create(Registries.SCHEMATIC_INDEX_PACK_TYPES).fieldOf("type").forGetter(Pack::type),
        SCHEMATICS_CODEC.fieldOf("schematics").forGetter(Pack::schematics)
    ).apply(instance, Pack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Pack> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Pack>> MAP_STREAM_CODEC =
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, STREAM_CODEC);

    public Pack(@NotNull final String id, @NotNull final String name, @NotNull final Holder<PackType> type)
    {
        this(id, name, type, Collections.emptySet());
    }

    public Pack
    {
        schematics = Set.copyOf(schematics);
    }

    public static Pack load(final @NotNull CompoundTag compound, final @NotNull HolderLookup.Provider provider)
    {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), compound).getOrThrow();
    }

    public CompoundTag save(final @NotNull HolderLookup.Provider provider)
    {
        return (CompoundTag) CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    /**
     * Returns a new {@link Pack} with the given schematic added, replacing any existing schematic with the same identity.
     */
    public Pack withSchematic(final @NotNull PackSchematic schematic)
    {
        final Set<PackSchematic> updated = new HashSet<>(schematics);
        updated.remove(schematic);
        updated.add(schematic);
        return new Pack(id, name, type, updated);
    }
}
