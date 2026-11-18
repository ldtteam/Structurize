package com.ldtteam.structurize.blueprints.conversion;

import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.mojang.datafixers.DSL.TypeReference;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ldtteam.structurize.api.constants.Constants.MOD_ID;

/**
 * Common, registry-independent blueprint assembly for foreign schematic converters.
 * Format-specific parsing stays in the respective importer; serialization stays in BlueprintUtil.
 */
final class SchematicConversionUtils
{
    static final         int    MAX_BLOCK_COUNT   = 16 * 1024 * 1024;
    private static final String LIGHT_PLACEHOLDER = MOD_ID + ":blocksubstitution";

    final         int                        sizeX;
    final         int                        sizeY;
    final         int                        sizeZ;
    final         short[][][]                blocks;
    private final ListTag                    palette        = new ListTag();
    private final Map<CompoundTag, Short>    paletteIndices = new HashMap<>();
    private final Map<BlockPos, CompoundTag> blockEntities  = new LinkedHashMap<>();
    private final ListTag                    entities       = new ListTag();

    SchematicConversionUtils(final int sizeX, final int sizeY, final int sizeZ)
    {
        validateSize(sizeX, sizeY, sizeZ);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        // Palette index zero is the light placeholder, including gaps between Litematica regions.
        final CompoundTag placeholder = new CompoundTag();
        placeholder.putString("Name", LIGHT_PLACEHOLDER);
        addState(placeholder, currentVersion());
        blocks = new short[sizeY][sizeZ][sizeX];
    }

    static void validateSize(final int sizeX, final int sizeY, final int sizeZ)
    {
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0
            || sizeX > Short.MAX_VALUE || sizeY > Short.MAX_VALUE || sizeZ > Short.MAX_VALUE
            || (long) sizeX * sizeY * sizeZ > MAX_BLOCK_COUNT)
        {
            throw new IllegalArgumentException("Invalid or oversized schematic dimensions: " + sizeX + ", " + sizeY + ", " + sizeZ);
        }
    }

    static int currentVersion()
    {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    static int dataVersion(final CompoundTag source, final String key, final int fallback)
    {
        if (source.contains(key) && !source.contains(key, Tag.TAG_INT))
        {
            throw new IllegalArgumentException("Invalid " + key);
        }
        final int version = source.contains(key) ? source.getInt(key) : fallback;
        if (version < 0 || version > currentVersion())
        {
            throw new IllegalArgumentException("Unsupported Minecraft data version: " + version);
        }
        return version;
    }

    static CompoundTag compound(final CompoundTag source, final String key)
    {
        if (!source.contains(key, Tag.TAG_COMPOUND))
        {
            throw new IllegalArgumentException("Missing or invalid compound: " + key);
        }
        return source.getCompound(key);
    }

    static ListTag list(final CompoundTag source, final String key, final int type)
    {
        if (!(source.get(key) instanceof ListTag list) || (!list.isEmpty() && list.getElementType() != type))
        {
            throw new IllegalArgumentException("Missing or invalid list: " + key);
        }
        return list;
    }

    static ListTag optionalList(final CompoundTag source, final String key)
    {
        return source.contains(key) ? list(source, key, Tag.TAG_COMPOUND) : new ListTag();
    }

    static int[] intVector(final CompoundTag source, final String key)
    {
        if (!source.contains(key, Tag.TAG_INT_ARRAY) || source.getIntArray(key).length != 3)
        {
            throw new IllegalArgumentException("Expected three integer coordinates in " + key);
        }
        return source.getIntArray(key);
    }

    static int[] blockPosition(final CompoundTag source)
    {
        if (!source.contains("x", Tag.TAG_INT) || !source.contains("y", Tag.TAG_INT) || !source.contains("z", Tag.TAG_INT))
        {
            throw new IllegalArgumentException("Missing block coordinates");
        }
        return new int[] {source.getInt("x"), source.getInt("y"), source.getInt("z")};
    }

    static CompoundTag fix(final CompoundTag data, final TypeReference type, final int version)
    {
        return DataFixerUtils.runDataFixer(data.copy(), type, version, currentVersion());
    }

    short addState(final CompoundTag input, final int version)
    {
        if (!input.contains("Name", Tag.TAG_STRING) || input.getString("Name").isEmpty())
        {
            throw new IllegalArgumentException("Missing block name in schematic palette");
        }
        final CompoundTag state = fix(version <= 1343 ? LegacyBlockStateFixer.normalizeSkull(input) : input, References.BLOCK_STATE, version);
        final ResourceLocation id = ResourceLocation.tryParse(state.getString("Name"));
        if (id == null)
        {
            throw new IllegalArgumentException("Missing or invalid block name: " + state);
        }
        state.putString("Name", id.toString());
        if (id.toString().equals("minecraft:structure_void"))
        {
            state.putString("Name", LIGHT_PLACEHOLDER);
            state.remove("Properties");
        }
        final Short existing = paletteIndices.get(state);
        if (existing != null)
        {
            return existing;
        }
        if (palette.size() >= Short.MAX_VALUE)
        {
            throw new IllegalArgumentException("Schematic palette is too large for blueprint v1");
        }
        final short index = (short) palette.size();
        palette.add(state);
        paletteIndices.put(state, index);
        return index;
    }

    void checkPosition(final int x, final int y, final int z)
    {
        if (x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
        {
            throw new IllegalArgumentException("Block outside schematic: " + x + ", " + y + ", " + z);
        }
    }

    void setBlock(final int x, final int y, final int z, final short state)
    {
        if (state == 0)
        {
            // A structure void also preserves blocks imported from an earlier overlapping region.
            return;
        }
        blocks[y][z][x] = state;
        // Later Litematica regions replace both the block and any earlier block entity at this cell.
        if (!blockEntities.isEmpty())
        {
            blockEntities.remove(new BlockPos(x, y, z));
        }
    }

    /**
     * Read-only palette view. Callers must copy before changing a state used as a palette map key.
     */
    CompoundTag stateAt(final int x, final int y, final int z)
    {
        return palette.getCompound(blocks[y][z][x]);
    }

    void addBlockEntity(final CompoundTag input, final int x, final int y, final int z, final int version)
    {
        checkPosition(x, y, z);
        // Pre-flattening tile properties also occur in old Litematica and Sponge exports.
        if (version <= 1343 && LegacyBlockStateFixer.fixBlockEntityState(this, input, new int[] {x, y, z}))
        {
            return;
        }
        final CompoundTag tile = fix(input, References.BLOCK_ENTITY, version);
        tile.putInt("x", x);
        tile.putInt("y", y);
        tile.putInt("z", z);
        blockEntities.put(new BlockPos(x, y, z), tile);
    }

    void addEntity(final CompoundTag input, final int dx, final int dy, final int dz, final int version)
    {
        final CompoundTag entity = fix(input, References.ENTITY, version);
        moveEntity(entity, dx, dy, dz);
        entities.add(entity);
    }

    private static void moveEntity(final CompoundTag entity, final int dx, final int dy, final int dz)
    {
        final ListTag pos = list(entity, "Pos", Tag.TAG_DOUBLE);
        if (pos.size() != 3)
        {
            throw new IllegalArgumentException("Expected three entity coordinates");
        }
        final int[] offset = {dx, dy, dz};
        for (int i = 0; i < 3; i++)
        {
            final double coordinate = pos.getDouble(i) + offset[i];
            if (!Double.isFinite(coordinate))
            {
                throw new IllegalArgumentException("Invalid entity position");
            }
            pos.set(i, DoubleTag.valueOf(coordinate));
        }
        if (entity.contains("TileX", Tag.TAG_ANY_NUMERIC))
        {
            entity.putInt("TileX", Math.addExact(entity.getInt("TileX"), dx));
            entity.putInt("TileY", Math.addExact(entity.getInt("TileY"), dy));
            entity.putInt("TileZ", Math.addExact(entity.getInt("TileZ"), dz));
        }
        for (final Tag passenger : optionalList(entity, "Passengers"))
        {
            moveEntity((CompoundTag) passenger, dx, dy, dz);
        }
    }

    @Nullable
    BlockPos anchor(final long x, final long y, final long z)
    {
        // Blueprint anchors must refer to a cell inside the volume. External paste origins cannot be represented.
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ
            ? new BlockPos((int) x, (int) y, (int) z) : null;
    }

    CompoundTag toBlueprint(final String name, final String author, @Nullable final BlockPos anchor)
    {
        final Set<String> requiredMods = new TreeSet<>();
        for (final Tag state : palette)
        {
            addRequiredMod(requiredMods, ((CompoundTag) state).getString("Name"));
        }
        final ListTag tiles = new ListTag();
        for (final CompoundTag tile : blockEntities.values())
        {
            tiles.add(tile);
            addRequiredMod(requiredMods, tile.getString("id"));
        }
        collectEntityMods(requiredMods, entities);
        return BlueprintUtil.writeBlueprintToNBT(sizeX, sizeY, sizeZ, blocks, palette, tiles, entities, requiredMods,
            name, author.isEmpty() ? new String[0] : new String[] {author}, anchor);
    }

    private static void collectEntityMods(final Set<String> mods, final ListTag entities)
    {
        for (final Tag entry : entities)
        {
            final CompoundTag entity = (CompoundTag) entry;
            addRequiredMod(mods, entity.getString("id"));
            collectEntityMods(mods, optionalList(entity, "Passengers"));
        }
    }

    private static void addRequiredMod(final Set<String> mods, final String name)
    {
        final ResourceLocation id = ResourceLocation.tryParse(name);
        if (id != null && !id.getNamespace().equals("minecraft") && !id.getNamespace().equals(MOD_ID))
        {
            mods.add(id.getNamespace());
        }
    }
}
