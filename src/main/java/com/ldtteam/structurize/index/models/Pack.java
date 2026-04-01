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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pack
{
    public static final Codec<Pack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(p -> p.id),
        Codec.STRING.fieldOf("name").forGetter(p -> p.name),
        RegistryFixedCodec.create(Registries.SCHEMATIC_INDEX_PACK_TYPES).fieldOf("type").forGetter(p -> p.type),
        PackSchematic.CODEC.listOf().fieldOf("schematics").forGetter(p -> p.schematics)
    ).apply(instance, Pack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Pack> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Pack>> MAP_STREAM_CODEC =
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, STREAM_CODEC);

    @NotNull
    private final String id;

    @NotNull
    private final String name;

    @NotNull
    private final Holder<PackType> type;

    @NotNull
    private final List<PackSchematic> schematics;

    public Pack(final @NotNull String id, final @NotNull String name, final @NotNull Holder<PackType> type)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.schematics = new ArrayList<>();
    }

    private Pack(final @NotNull String id, final @NotNull String name, final @NotNull Holder<PackType> type, final @NotNull List<PackSchematic> schematics)
    {
        this.id = id;
        this.name = name;
        this.type = type;
        this.schematics = new ArrayList<>(schematics);
    }

    public static Pack load(final @NotNull CompoundTag compound, final @NotNull HolderLookup.Provider provider)
    {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), compound).getOrThrow();
    }

    public CompoundTag save(final @NotNull HolderLookup.Provider provider)
    {
        return (CompoundTag) CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    @NotNull
    public String getId()
    {
        return id;
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    @NotNull
    public Holder<PackType> getType()
    {
        return type;
    }

    @NotNull
    public List<PackSchematic> getSchematics()
    {
        return schematics;
    }

    public void addSchematic(final @NotNull PackSchematic schematic)
    {
        schematics.add(schematic);
    }
}
