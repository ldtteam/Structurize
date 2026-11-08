package com.ldtteam.structurize.blueprints.conversion;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the legacy MCEdit/WorldEdit Alpha .schematic format (numeric block IDs and metadata).
 * Vanilla IDs and vanilla entries in SchematicaMapping are supported. Modded numeric IDs cannot be
 * translated to modern block states reliably and cause the import to fail instead of becoming air.
 */
public final class LegacySchematicImporter
{
    private static final int FLATTENED_VERSION = 1519;

    private LegacySchematicImporter()
    {
    }

    /**
     * Convert an Alpha schematic without modifying the input.
     *
     * @param input source NBT.
     * @param name  blueprint name.
     * @return blueprint v1 NBT.
     */
    public static CompoundTag convert(final CompoundTag input, final String name)
    {
        if (input.contains("Materials") && !input.getString("Materials").equals("Alpha"))
        {
            throw new IllegalArgumentException("Only Alpha legacy schematics are supported");
        }
        final int dataVersion = SchematicConversionUtils.dataVersion(input, "DataVersion", 100);
        final SchematicConversionUtils result = new SchematicConversionUtils(
            dimension(input, "Width"), dimension(input, "Height"), dimension(input, "Length"));
        final int volume = result.sizeX * result.sizeY * result.sizeZ;
        final byte[] ids = byteArray(input, "Blocks");
        final byte[] metadata = byteArray(input, "Data");
        final byte[] extra = input.contains("AddBlocks") ? byteArray(input, "AddBlocks") : new byte[0];
        if (ids.length != volume || metadata.length != volume || extra.length > (volume + 1) / 2)
        {
            throw new IllegalArgumentException("Legacy schematic block arrays do not match its dimensions");
        }
        final Map<Integer, Integer> idMapping = readMapping(input);
        final Map<Integer, Short> states = new HashMap<>();
        int index = 0;
        for (int y = 0; y < result.sizeY; y++)
        {
            for (int z = 0; z < result.sizeZ; z++)
            {
                for (int x = 0; x < result.sizeX; x++, index++)
                {
                    int id = ids[index] & 0xFF;
                    if (index / 2 < extra.length)
                    {
                        id |= ((extra[index / 2] >>> ((index & 1) * 4)) & 0xF) << 8;
                    }
                    if (!idMapping.isEmpty() && (id != 0 || idMapping.containsKey(0)))
                    {
                        if (!idMapping.containsKey(id))
                        {
                            throw new IllegalArgumentException("Unmapped legacy block ID: " + id);
                        }
                        id = idMapping.get(id);
                    }
                    if (id < 0 || id > 255 || (id != 0 && BlockStateData.upgradeBlock(id << 4).equals("minecraft:air")))
                    {
                        throw new IllegalArgumentException("Unsupported legacy block ID: " + id + " (modded IDs require a modern schematic export)");
                    }
                    final int combined = (id << 4) | (metadata[index] & 0xF);
                    final short state = states.computeIfAbsent(combined,
                        key -> result.addState(LegacyBlockStateFixer.normalizeSkull(asTag(BlockStateData.getTag(key))), FLATTENED_VERSION));
                    result.setBlock(x, y, z, state);
                }
            }
        }
        LegacyBlockStateFixer.fixMultiBlockStates(result);
        for (final Tag entry : SchematicConversionUtils.optionalList(input, "TileEntities"))
        {
            final CompoundTag tile = ((CompoundTag) entry).copy();
            final int[] pos = SchematicConversionUtils.blockPosition(tile);
            result.checkPosition(pos[0], pos[1], pos[2]);
            result.addBlockEntity(tile, pos[0], pos[1], pos[2], dataVersion);
        }
        for (final Tag entry : SchematicConversionUtils.optionalList(input, "Entities"))
        {
            result.addEntity((CompoundTag) entry, 0, 0, 0, dataVersion);
        }
        BlockPos anchor = null;
        if (input.contains("WEOffsetX", Tag.TAG_INT) && input.contains("WEOffsetY", Tag.TAG_INT) && input.contains("WEOffsetZ", Tag.TAG_INT))
        {
            anchor = result.anchor(-(long) input.getInt("WEOffsetX"), -(long) input.getInt("WEOffsetY"), -(long) input.getInt("WEOffsetZ"));
        }
        return result.toBlueprint(name, input.getString("Author"), anchor);
    }

    private static Map<Integer, Integer> readMapping(final CompoundTag input)
    {
        final Map<Integer, Integer> mapping = new HashMap<>();
        if (input.contains("SchematicaMapping"))
        {
            final CompoundTag source = SchematicConversionUtils.compound(input, "SchematicaMapping");
            for (final String name : source.getAllKeys())
            {
                if (!source.contains(name, Tag.TAG_ANY_NUMERIC))
                {
                    throw new IllegalArgumentException("Invalid Schematica block mapping: " + name);
                }
                final String namespaced = name.contains(":") ? name : "minecraft:" + name;
                final int vanillaId = EntityBlockStateFix.getBlockId(namespaced);
                mapping.put(source.getInt(name), vanillaId == 0 && !namespaced.equals("minecraft:air") ? -1 : vanillaId);
            }
        }
        return mapping;
    }

    private static CompoundTag asTag(final Dynamic<?> dynamic)
    {
        return (CompoundTag) dynamic.convert(NbtOps.INSTANCE).getValue();
    }

    private static int dimension(final CompoundTag input, final String key)
    {
        if (!input.contains(key, Tag.TAG_SHORT))
        {
            throw new IllegalArgumentException("Missing legacy schematic dimension: " + key);
        }
        return input.getShort(key) & 0xFFFF;
    }

    private static byte[] byteArray(final CompoundTag input, final String key)
    {
        if (!input.contains(key, Tag.TAG_BYTE_ARRAY))
        {
            throw new IllegalArgumentException("Missing legacy schematic array: " + key);
        }
        return input.getByteArray(key);
    }
}
