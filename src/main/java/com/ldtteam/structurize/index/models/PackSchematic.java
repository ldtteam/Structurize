package com.ldtteam.structurize.index.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PackSchematic
{
    public static final Codec<PackSchematic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("path").forGetter(s -> s.path),
        Codec.STRING.fieldOf("name").forGetter(s -> s.name),
        Codec.INT.fieldOf("level").forGetter(s -> s.level),
        BlockPos.CODEC.fieldOf("pos1").forGetter(s -> s.pos1),
        BlockPos.CODEC.fieldOf("pos2").forGetter(s -> s.pos2),
        BlockPos.CODEC.optionalFieldOf("anchor").forGetter(s -> Optional.ofNullable(s.anchor)),
        Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(s -> s.world)
    ).apply(instance, (path, name, level, pos1, pos2, anchor, world) -> new PackSchematic(path, name, level, pos1, pos2, anchor.orElse(null), world)));


    @NotNull
    private final String path;

    @NotNull
    private final String name;

    private final int level;

    @NotNull
    private final BlockPos pos1;

    @NotNull
    private final BlockPos pos2;

    @Nullable
    private final BlockPos anchor;

    @NotNull
    private final ResourceKey<Level> world;

    public PackSchematic(
        final @NotNull String path,
        final @NotNull String name,
        final int level,
        final @NotNull BlockPos pos1,
        final @NotNull BlockPos pos2,
        final @Nullable BlockPos anchor,
        final @NotNull ResourceKey<Level> world)
    {
        this.path = path;
        this.name = name;
        this.level = level;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.anchor = anchor;
        this.world = world;
    }

    @NotNull
    public String getPath()
    {
        return path;
    }

    @NotNull
    public String getName()
    {
        return name;
    }

    public int getLevel()
    {
        return level;
    }

    @NotNull
    public BlockPos getPos1()
    {
        return pos1;
    }

    @NotNull
    public BlockPos getPos2()
    {
        return pos2;
    }

    @Nullable
    public BlockPos getAnchor()
    {
        return anchor;
    }

    @NotNull
    public ResourceKey<Level> getWorld()
    {
        return world;
    }
}
