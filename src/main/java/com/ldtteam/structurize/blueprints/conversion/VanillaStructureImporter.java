package com.ldtteam.structurize.blueprints.conversion;

import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.References;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;

import static com.ldtteam.structurize.api.constants.Constants.MOD_ID;

/**
 * Converts Java Edition structure templates into blueprint v1 NBT, without a world or block registry.
 * Alternative palettes are resolved deterministically by using the first palette.
 */
public final class VanillaStructureImporter
{
    private static final String LIGHT_PLACEHOLDER = MOD_ID + ":blocksubstitution";

    /**
     * Avoid allocating an unbounded dense blueprint from a small, sparse structure file.
     */
    private static final int MAX_BLOCK_COUNT = 16 * 1024 * 1024;

    private VanillaStructureImporter()
    {
    }

    /**
     * Convert a structure template. The input is never modified; missing cells and structure void
     * become light placeholders, while explicit air remains air. Anchor selection is left to Blueprint.
     *
     * @param input the vanilla structure NBT.
     * @param name  the display name of the imported blueprint.
     * @return the blueprint v1 NBT.
     * @throws IllegalArgumentException if the input is invalid, too large, or from a newer Minecraft version.
     */
    public static CompoundTag convert(final CompoundTag input, final String name)
    {
        // Validate the format before attempting a data fix on an arbitrary .nbt file.
        readSize(input);
        readList(input, "blocks", Tag.TAG_COMPOUND);
        readPalette(input);

        final int currentVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        // Match vanilla StructureTemplateManager's fallback for structures without DataVersion.
        if (input.contains("DataVersion") && !input.contains("DataVersion", Tag.TAG_INT))
        {
            throw new IllegalArgumentException("Invalid structure DataVersion");
        }
        final int sourceVersion = input.contains("DataVersion", Tag.TAG_INT) ? input.getInt("DataVersion") : 500;
        if (sourceVersion < 0 || sourceVersion > currentVersion)
        {
            throw new IllegalArgumentException("Unsupported structure DataVersion: " + sourceVersion);
        }

        final CompoundTag source = sourceVersion == currentVersion
            ? input.copy()
            : DataFixerUtils.runDataFixer(input.copy(), References.STRUCTURE, sourceVersion, currentVersion);
        final int[] size = readSize(source);
        final int sourcePaletteSize = readPalette(source).size();
        final ListTag palette = readPalette(source).copy();
        if (palette.size() > Short.MAX_VALUE)
        {
            throw new IllegalArgumentException("Structure palette is too large for blueprint v1");
        }

        int placeholderIndex = -1;
        final Set<String> requiredMods = new TreeSet<>();
        for (int i = 0; i < palette.size(); i++)
        {
            final CompoundTag state = palette.getCompound(i);
            if (!state.contains("Name", Tag.TAG_STRING) || state.getString("Name").isEmpty())
            {
                throw new IllegalArgumentException("Missing block name in palette entry " + i);
            }
            if (state.getString("Name").equals("minecraft:structure_void") || state.getString("Name").equals("structure_void"))
            {
                final CompoundTag placeholder = new CompoundTag();
                placeholder.putString("Name", LIGHT_PLACEHOLDER);
                palette.set(i, placeholder);
            }
            if (palette.getCompound(i).getString("Name").equals(LIGHT_PLACEHOLDER))
            {
                placeholderIndex = i;
            }
            addRequiredMod(requiredMods, palette.getCompound(i).getString("Name"));
        }
        if (placeholderIndex == -1)
        {
            if (palette.size() == Short.MAX_VALUE)
            {
                throw new IllegalArgumentException("No room for a placeholder in the structure palette");
            }
            placeholderIndex = palette.size();
            final CompoundTag placeholder = new CompoundTag();
            placeholder.putString("Name", LIGHT_PLACEHOLDER);
            palette.add(placeholder);
        }
        final int volume = size[0] * size[1] * size[2];
        final short[][][] blockData = new short[size[1]][size[2]][size[0]];
        for (final short[][] layer : blockData)
        {
            for (final short[] row : layer)
            {
                Arrays.fill(row, (short) placeholderIndex);
            }
        }
        final BitSet occupied = new BitSet(volume);
        final ListTag tileEntities = new ListTag();
        final ListTag blocks = readList(source, "blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); i++)
        {
            final CompoundTag block = blocks.getCompound(i);
            final ListTag pos = readVector(block, "pos", Tag.TAG_INT);
            final int x = pos.getInt(0), y = pos.getInt(1), z = pos.getInt(2);
            if (x < 0 || x >= size[0] || y < 0 || y >= size[1] || z < 0 || z >= size[2])
            {
                throw new IllegalArgumentException("Block position is outside the structure: " + pos);
            }
            final int index = (y * size[2] + z) * size[0] + x;
            if (occupied.get(index))
            {
                throw new IllegalArgumentException("Duplicate block position: " + pos);
            }
            occupied.set(index);
            // A block may only reference the source palette, not the placeholder appended above.
            final int state = block.getInt("state");
            if (!block.contains("state", Tag.TAG_INT) || state < 0 || state >= sourcePaletteSize)
            {
                throw new IllegalArgumentException("Invalid block palette index: " + state);
            }
            blockData[y][z][x] = (short) state;
            if (block.contains("nbt"))
            {
                if (!block.contains("nbt", Tag.TAG_COMPOUND))
                {
                    throw new IllegalArgumentException("Invalid block entity NBT at " + pos);
                }
                final CompoundTag tile = block.getCompound("nbt").copy();
                tile.putInt("x", x);
                tile.putInt("y", y);
                tile.putInt("z", z);
                tileEntities.add(tile);
                addRequiredMod(requiredMods, tile.getString("id"));
            }
        }

        final ListTag entities = new ListTag();
        final ListTag sourceEntities = source.contains("entities") ? readList(source, "entities", Tag.TAG_COMPOUND) : new ListTag();
        for (int i = 0; i < sourceEntities.size(); i++)
        {
            final CompoundTag entry = sourceEntities.getCompound(i);
            if (!entry.contains("nbt", Tag.TAG_COMPOUND))
            {
                throw new IllegalArgumentException("Missing entity NBT at index " + i);
            }
            final ListTag pos = readVector(entry, "pos", Tag.TAG_DOUBLE);
            for (int axis = 0; axis < 3; axis++)
            {
                if (!Double.isFinite(pos.getDouble(axis)))
                {
                    throw new IllegalArgumentException("Invalid entity position: " + pos);
                }
            }
            final ListTag blockPos = readVector(entry, "blockPos", Tag.TAG_INT);
            final CompoundTag entity = entry.getCompound("nbt").copy();
            entity.put("Pos", pos.copy());
            // Match scanned blueprints, including relative attachment coordinates for paintings and frames.
            entity.putInt("TileX", blockPos.getInt(0));
            entity.putInt("TileY", blockPos.getInt(1));
            entity.putInt("TileZ", blockPos.getInt(2));
            entities.add(entity);
            addRequiredMod(requiredMods, entity.getString("id"));
        }

        final String[] architects = source.contains("author", Tag.TAG_STRING)
            ? new String[] {source.getString("author")}
            : new String[0];
        return BlueprintUtil.writeBlueprintToNBT(
            size[0], size[1], size[2], blockData, palette, tileEntities, entities, requiredMods,
            name, architects, null);
    }

    private static int[] readSize(final CompoundTag source)
    {
        final ListTag size = readVector(source, "size", Tag.TAG_INT);
        long volume = 1;
        final int[] dimensions = new int[3];
        for (int i = 0; i < dimensions.length; i++)
        {
            dimensions[i] = size.getInt(i);
            if (dimensions[i] <= 0 || dimensions[i] > Short.MAX_VALUE)
            {
                throw new IllegalArgumentException("Invalid blueprint dimension: " + dimensions[i]);
            }
            volume *= dimensions[i];
        }
        if (volume > MAX_BLOCK_COUNT)
        {
            throw new IllegalArgumentException("Structure exceeds " + MAX_BLOCK_COUNT + " blocks");
        }
        return dimensions;
    }

    private static ListTag readPalette(final CompoundTag source)
    {
        if (source.contains("palettes"))
        {
            final ListTag palettes = readList(source, "palettes", Tag.TAG_LIST);
            if (palettes.isEmpty() || (!palettes.getList(0).isEmpty() && palettes.getList(0).getElementType() != Tag.TAG_COMPOUND))
            {
                throw new IllegalArgumentException("Invalid structure palettes");
            }
            return palettes.getList(0);
        }
        return readList(source, "palette", Tag.TAG_COMPOUND);
    }

    private static ListTag readVector(final CompoundTag source, final String key, final int type)
    {
        final ListTag vector = readList(source, key, type);
        if (vector.size() != 3)
        {
            throw new IllegalArgumentException("Expected three coordinates in " + key);
        }
        return vector;
    }

    private static ListTag readList(final CompoundTag source, final String key, final int type)
    {
        if (!(source.get(key) instanceof ListTag list) || (!list.isEmpty() && list.getElementType() != type))
        {
            throw new IllegalArgumentException("Missing or invalid structure list: " + key);
        }
        return list;
    }

    private static void addRequiredMod(final Set<String> mods, final String id)
    {
        if (!id.isEmpty())
        {
            final int separator = id.indexOf(':');
            final String modId = separator == -1 ? "minecraft" : id.substring(0, separator);
            if (!modId.equals("minecraft") && !modId.equals(MOD_ID))
            {
                mods.add(modId);
            }
        }
    }
}
