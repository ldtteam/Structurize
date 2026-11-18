package com.ldtteam.structurize.blueprints.conversion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

/**
 * Converts Litematica versions 1 through 7 (.litematic), combining regions into one dense blueprint.
 * Regions are applied in name order; later regions replace earlier blocks, except at structure voids.
 * Scheduled ticks and placement settings are not part of the blueprint format.
 */
public final class LitematicaSchematicImporter
{
    private LitematicaSchematicImporter()
    {
    }

    /**
     * Convert a Litematica schematic without modifying the input.
     *
     * @param input source NBT.
     * @param name  blueprint name.
     * @return blueprint v1 NBT.
     */
    public static CompoundTag convert(final CompoundTag input, final String name)
    {
        final int version = input.getInt("Version");
        if (version < 1 || version > 7)
        {
            throw new IllegalArgumentException("Unsupported Litematica version: " + version);
        }
        // Original 1.12 files did not store MinecraftDataVersion.
        final int dataVersion = SchematicConversionUtils.dataVersion(input, "MinecraftDataVersion", 1343);
        final CompoundTag sourceRegions = SchematicConversionUtils.compound(input, "Regions");
        final List<Region> regions = new ArrayList<>();
        final long[] min = {Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
        final long[] max = {Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE};
        for (final String regionName : new TreeSet<>(sourceRegions.getAllKeys()))
        {
            final CompoundTag regionTag = SchematicConversionUtils.compound(sourceRegions, regionName);
            final int[] position = SchematicConversionUtils.blockPosition(SchematicConversionUtils.compound(regionTag, "Position"));
            final int[] signedSize = SchematicConversionUtils.blockPosition(SchematicConversionUtils.compound(regionTag, "Size"));
            final int[] size = new int[3];
            final long[] regionMin = new long[3];
            for (int axis = 0; axis < 3; axis++)
            {
                final long dimension = Math.abs((long) signedSize[axis]);
                if (dimension == 0 || dimension > Short.MAX_VALUE)
                {
                    throw new IllegalArgumentException("Invalid Litematica region size: " + regionName);
                }
                size[axis] = (int) dimension;
                // A size of -3 includes the origin and the two cells preceding it.
                regionMin[axis] = (long) position[axis] + Math.min(0L, (long) signedSize[axis] + 1);
                min[axis] = Math.min(min[axis], regionMin[axis]);
                max[axis] = Math.max(max[axis], regionMin[axis] + dimension);
            }
            SchematicConversionUtils.validateSize(size[0], size[1], size[2]);
            regions.add(new Region(regionTag, position, regionMin, size));
        }
        if (regions.isEmpty())
        {
            throw new IllegalArgumentException("Litematica schematic has no regions");
        }
        final SchematicConversionUtils result = new SchematicConversionUtils(
            Math.toIntExact(max[0] - min[0]), Math.toIntExact(max[1] - min[1]), Math.toIntExact(max[2] - min[2]));
        final Set<Tag> entityIds = new HashSet<>();
        for (final Region region : regions)
        {
            importRegion(result, region, min, version, dataVersion, entityIds);
        }
        if (dataVersion <= 1343)
        {
            LegacyBlockStateFixer.fixMultiBlockStates(result);
        }
        return result.toBlueprint(name, input.getCompound("Metadata").getString("Author"), result.anchor(-min[0], -min[1], -min[2]));
    }

    private static void importRegion(
        final SchematicConversionUtils result, final Region region, final long[] min,
        final int version, final int dataVersion, final Set<Tag> entityIds)
    {
        final ListTag sourcePalette = SchematicConversionUtils.list(region.tag, "BlockStatePalette", Tag.TAG_COMPOUND);
        if (sourcePalette.isEmpty() || sourcePalette.size() > Short.MAX_VALUE)
        {
            throw new IllegalArgumentException("Invalid Litematica block palette size: " + sourcePalette.size());
        }
        final short[] palette = new short[sourcePalette.size()];
        for (int i = 0; i < palette.length; i++)
        {
            palette[i] = result.addState(sourcePalette.getCompound(i), dataVersion);
        }
        if (!region.tag.contains("BlockStates", Tag.TAG_LONG_ARRAY))
        {
            throw new IllegalArgumentException("Missing Litematica block states");
        }
        final long[] data = region.tag.getLongArray("BlockStates");
        final int bits = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.length - 1));
        final long mask = (1L << bits) - 1;
        final long blockCount = (long) region.size[0] * region.size[1] * region.size[2];
        if (data.length != (blockCount * bits + 63) / 64)
        {
            throw new IllegalArgumentException("Litematica block storage length does not match the region size");
        }
        final int dx = Math.toIntExact(region.min[0] - min[0]);
        final int dy = Math.toIntExact(region.min[1] - min[1]);
        final int dz = Math.toIntExact(region.min[2] - min[2]);
        long bitIndex = 0;
        for (int y = 0; y < region.size[1]; y++)
        {
            for (int z = 0; z < region.size[2]; z++)
            {
                for (int x = 0; x < region.size[0]; x++, bitIndex += bits)
                {
                    final int word = (int) (bitIndex >>> 6);
                    final int shift = (int) (bitIndex & 63);
                    long value = data[word] >>> shift;
                    // Litematica's continuous bit stream allows an entry to straddle two longs.
                    if (shift + bits > 64)
                    {
                        value |= data[word + 1] << (64 - shift);
                    }
                    final int index = (int) (value & mask);
                    if (index >= palette.length)
                    {
                        throw new IllegalArgumentException("Invalid Litematica palette index: " + index);
                    }
                    result.setBlock(x + dx, y + dy, z + dz, palette[index]);
                }
            }
        }
        for (final Tag entry : SchematicConversionUtils.optionalList(region.tag, "TileEntities"))
        {
            final CompoundTag wrapper = (CompoundTag) entry;
            final int[] pos = SchematicConversionUtils.blockPosition(wrapper);
            for (int axis = 0; axis < 3; axis++)
            {
                if (pos[axis] < 0 || pos[axis] >= region.size[axis])
                {
                    throw new IllegalArgumentException("Litematica block entity outside its region");
                }
            }
            final CompoundTag tile = version == 1 ? SchematicConversionUtils.compound(wrapper, "TileNBT") : wrapper;
            result.addBlockEntity(tile, pos[0] + dx, pos[1] + dy, pos[2] + dz, dataVersion);
        }
        for (final Tag entry : SchematicConversionUtils.optionalList(region.tag, "Entities"))
        {
            final CompoundTag wrapper = (CompoundTag) entry;
            final CompoundTag entity = version == 1 ? SchematicConversionUtils.compound(wrapper, "EntityData").copy() : wrapper;
            if (version == 1)
            {
                final ListTag pos = new ListTag();
                for (final String axis : new String[] {"x", "y", "z"})
                {
                    if (!wrapper.contains(axis, Tag.TAG_ANY_NUMERIC))
                    {
                        throw new IllegalArgumentException("Missing Litematica entity coordinate: " + axis);
                    }
                    pos.add(DoubleTag.valueOf(wrapper.getDouble(axis)));
                }
                entity.put("Pos", pos);
            }
            // The same entity may have been captured in more than one overlapping region.
            if (entity.contains("UUID", Tag.TAG_INT_ARRAY) && !entityIds.add(entity.get("UUID")))
            {
                continue;
            }
            // Entity positions use the signed region origin; block entities use its minimum corner.
            result.addEntity(entity, Math.toIntExact((long) region.position[0] - min[0]),
                Math.toIntExact((long) region.position[1] - min[1]), Math.toIntExact((long) region.position[2] - min[2]), dataVersion);
        }
    }

    private record Region(
        CompoundTag tag,
        int[] position,
        long[] min,
        int[] size)
    {
    }
}
