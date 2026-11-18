package com.ldtteam.structurize.blueprints.conversion;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts Sponge schematic versions 1, 2 and 3 (.schem).
 * Biome data has no equivalent in the blueprint format and is not imported.
 *
 * @see <a href="https://github.com/SpongePowered/Schematic-Specification">Sponge schematic specification</a>
 */
public final class SpongeSchematicImporter
{
    private SpongeSchematicImporter()
    {
    }

    /**
     * Convert a Sponge schematic without modifying the input or resolving blocks against a registry.
     *
     * @param input source NBT.
     * @param name  blueprint name.
     * @return blueprint v1 NBT.
     */
    public static CompoundTag convert(final CompoundTag input, final String name)
    {
        final CompoundTag source = input.contains("Schematic", Tag.TAG_COMPOUND) ? input.getCompound("Schematic") : input;
        final int version = source.getInt("Version");
        if (version < 1 || version > 3)
        {
            throw new IllegalArgumentException("Unsupported Sponge schematic version: " + version);
        }
        // v1 predates DataVersion. Match WorldEdit's 1.13.2 fallback when it is absent.
        final int dataVersion = SchematicConversionUtils.dataVersion(source, "DataVersion", version == 1 ? 1631 : -1);
        final SchematicConversionUtils result = new SchematicConversionUtils(
            dimension(source, "Width"), dimension(source, "Height"), dimension(source, "Length"));
        final CompoundTag container = version == 3 ? SchematicConversionUtils.compound(source, "Blocks") : source;
        final CompoundTag sourcePalette = SchematicConversionUtils.compound(container, "Palette");
        if (sourcePalette.isEmpty())
        {
            // Global numeric state IDs are registry-specific and cannot be recovered from the file alone.
            throw new IllegalArgumentException("Sponge schematic has no local block palette");
        }
        final Map<Integer, Short> palette = new HashMap<>();
        for (final String state : sourcePalette.getAllKeys())
        {
            final int index = sourcePalette.getInt(state);
            if (!sourcePalette.contains(state, Tag.TAG_INT) || index < 0
                || palette.put(index, result.addState(parseState(state), dataVersion)) != null)
            {
                throw new IllegalArgumentException("Invalid or duplicate Sponge palette index: " + index);
            }
        }
        final String dataKey = version == 3 ? "Data" : "BlockData";
        if (!container.contains(dataKey, Tag.TAG_BYTE_ARRAY))
        {
            throw new IllegalArgumentException("Missing Sponge block data");
        }
        final byte[] data = container.getByteArray(dataKey);
        int cursor = 0;
        for (int y = 0; y < result.sizeY; y++)
        {
            for (int z = 0; z < result.sizeZ; z++)
            {
                for (int x = 0; x < result.sizeX; x++)
                {
                    int index = 0;
                    int shift = 0;
                    int value;
                    do
                    {
                        if (cursor >= data.length || shift >= 35)
                        {
                            throw new IllegalArgumentException("Truncated or oversized Sponge palette varint");
                        }
                        value = data[cursor++] & 0xFF;
                        if (shift == 28 && (value & 0xF8) != 0)
                        {
                            throw new IllegalArgumentException("Sponge palette varint exceeds a positive integer");
                        }
                        index |= (value & 0x7F) << shift;
                        shift += 7;
                    }
                    while ((value & 0x80) != 0);
                    final Short state = palette.get(index);
                    if (state == null)
                    {
                        throw new IllegalArgumentException("Unknown Sponge palette index: " + index);
                    }
                    result.setBlock(x, y, z, state);
                }
            }
        }
        if (cursor != data.length)
        {
            throw new IllegalArgumentException("Sponge block data exceeds the schematic volume");
        }
        if (dataVersion <= 1343)
        {
            LegacyBlockStateFixer.fixMultiBlockStates(result);
        }

        final String tilesKey = version == 1 && !container.contains("BlockEntities") ? "TileEntities" : "BlockEntities";
        for (final Tag entry : SchematicConversionUtils.optionalList(container, tilesKey))
        {
            final CompoundTag wrapper = (CompoundTag) entry;
            final int[] pos = SchematicConversionUtils.intVector(wrapper, "Pos");
            final CompoundTag tile = unwrap(wrapper, version);
            tile.remove("Pos");
            result.addBlockEntity(tile, pos[0], pos[1], pos[2], dataVersion);
        }
        for (final Tag entry : SchematicConversionUtils.optionalList(source, "Entities"))
        {
            final CompoundTag wrapper = (CompoundTag) entry;
            final CompoundTag entity = unwrap(wrapper, version);
            entity.put("Pos", SchematicConversionUtils.list(wrapper, "Pos", Tag.TAG_DOUBLE).copy());
            // Entity coordinates are relative to the minimum corner, without Offset applied.
            result.addEntity(entity, 0, 0, 0, dataVersion);
        }
        final CompoundTag metadata = source.getCompound("Metadata");
        BlockPos anchor = null;
        if (version < 3 && metadata.contains("WEOffsetX", Tag.TAG_INT)
            && metadata.contains("WEOffsetY", Tag.TAG_INT) && metadata.contains("WEOffsetZ", Tag.TAG_INT))
        {
            anchor = result.anchor(-(long) metadata.getInt("WEOffsetX"), -(long) metadata.getInt("WEOffsetY"), -(long) metadata.getInt("WEOffsetZ"));
        }
        else if (version == 3 && source.contains("Offset"))
        {
            final int[] offset = SchematicConversionUtils.intVector(source, "Offset");
            anchor = result.anchor(-(long) offset[0], -(long) offset[1], -(long) offset[2]);
        }
        return result.toBlueprint(name, metadata.getString("Author"), anchor);
    }

    private static int dimension(final CompoundTag source, final String key)
    {
        if (!source.contains(key, Tag.TAG_SHORT))
        {
            throw new IllegalArgumentException("Missing Sponge dimension: " + key);
        }
        return source.getShort(key) & 0xFFFF;
    }

    private static CompoundTag unwrap(final CompoundTag wrapper, final int version)
    {
        final CompoundTag data = version == 3
            ? (wrapper.contains("Data") ? SchematicConversionUtils.compound(wrapper, "Data").copy() : new CompoundTag())
            : wrapper.copy();
        if (!wrapper.contains("Id", Tag.TAG_STRING) || wrapper.getString("Id").isEmpty())
        {
            throw new IllegalArgumentException("Missing Sponge entity Id");
        }
        data.remove("Id");
        data.remove("ContentVersion");
        data.putString("id", wrapper.getString("Id"));
        return data;
    }

    private static CompoundTag parseState(final String value)
    {
        final int start = value.indexOf('[');
        final CompoundTag state = new CompoundTag();
        state.putString("Name", start == -1 ? value : value.substring(0, start));
        if (start != -1)
        {
            if (!value.endsWith("]"))
            {
                throw new IllegalArgumentException("Invalid Sponge block state: " + value);
            }
            final CompoundTag properties = new CompoundTag();
            final String content = value.substring(start + 1, value.length() - 1);
            if (!content.isEmpty())
            {
                for (final String property : content.split(",", -1))
                {
                    final String[] pair = property.split("=", -1);
                    if (pair.length != 2 || pair[0].isEmpty() || pair[1].isEmpty() || properties.contains(pair[0]))
                    {
                        throw new IllegalArgumentException("Invalid Sponge block property: " + property);
                    }
                    properties.putString(pair[0], pair[1]);
                }
            }
            state.put("Properties", properties);
        }
        return state;
    }
}
