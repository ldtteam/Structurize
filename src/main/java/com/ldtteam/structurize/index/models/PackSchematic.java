package com.ldtteam.structurize.index.models;

import com.ldtteam.structurize.blueprints.v1.IBlueprintDetails;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable record representing a single schematic entry within a {@link Pack}.
 *
 * <p>Equality and hashing are based on {@link #path}, {@link #name}, and {@link #level} so that
 * replacing a schematic (e.g. after rescanning) correctly overwrites the old entry in a set.
 *
 * @param path            the relative folder path of the schematic file, may be empty for root-level schematics
 * @param name            the schematic file name (without extension)
 * @param level           the building level this schematic represents (1-based); defaults to 1 when omitted from storage
 * @param pos1            one corner of the bounding box in world space
 * @param pos2            the opposite corner of the bounding box in world space
 * @param anchor          the world position of the anchor block, or {@code null} if not set
 * @param validationState the most recent per-schematic validation result
 */
public record PackSchematic(
    String path,
    String name,
    int level,
    BlockPos pos1,
    BlockPos pos2,
    Optional<BlockPos> anchor,
    PackSchematicValidationState validationState) implements IBlueprintDetails
{
    public static final Codec<PackSchematic> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("path").forGetter(PackSchematic::path),
        Codec.STRING.fieldOf("name").forGetter(PackSchematic::name),
        Codec.INT.optionalFieldOf("level", 1).forGetter(PackSchematic::level),
        BlockPos.CODEC.fieldOf("pos1").forGetter(PackSchematic::pos1),
        BlockPos.CODEC.fieldOf("pos2").forGetter(PackSchematic::pos2),
        BlockPos.CODEC.optionalFieldOf("anchor").forGetter(PackSchematic::anchor),
        PackSchematicValidationState.CODEC.fieldOf("validation-state").forGetter(PackSchematic::validationState)
    ).apply(instance, PackSchematic::new));

    @Override
    public BlockPos getPos1()
    {
        return pos1;
    }

    @Override
    public BlockPos getPos2()
    {
        return pos2;
    }

    @Override
    public String getSchematicPath()
    {
        return path;
    }

    @Override
    public String getSchematicName()
    {
        return name;
    }

    @Override
    public Integer getSchematicLevel()
    {
        return level;
    }

    @Override
    public BlockPos getAnchor()
    {
        return anchor.orElse(null);
    }

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
