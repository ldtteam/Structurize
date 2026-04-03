package com.ldtteam.structurize.index.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public record PackSchematic(
    String path,
    String name,
    int level,
    BlockPos pos1,
    BlockPos pos2,
    @Nullable BlockPos anchor)
{
    public static final Codec<PackSchematic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("path").forGetter(PackSchematic::path),
        Codec.STRING.fieldOf("name").forGetter(PackSchematic::name),
        Codec.INT.fieldOf("level").forGetter(PackSchematic::level),
        BlockPos.CODEC.fieldOf("pos1").forGetter(PackSchematic::pos1),
        BlockPos.CODEC.fieldOf("pos2").forGetter(PackSchematic::pos2),
        BlockPos.CODEC.optionalFieldOf("anchor").forGetter(s -> Optional.ofNullable(s.anchor()))
    ).apply(instance, (path, name, level, pos1, pos2, anchor) -> new PackSchematic(path, name, level, pos1, pos2, anchor.orElse(null))));


    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof PackSchematic other))
        {
            return false;
        }
        return level == other.level && path.equals(other.path) && name.equals(other.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(path, name, level);
    }
}
