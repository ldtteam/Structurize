package com.ldtteam.structurize.blueprints.conversion;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.world.item.DyeColor;

/**
 * Block states whose pre-1.13 properties depended on neighboring blocks or block entities.
 * Shared by the legacy format importer and older Sponge/Litematica exports.
 */
final class LegacyBlockStateFixer
{
    private static final int FLATTENED_VERSION = 1519;

    private LegacyBlockStateFixer()
    {
    }

    static CompoundTag normalizeSkull(final CompoundTag input)
    {
        final String name = input.getString("Name");
        if (!name.equals("%%FILTER_ME%%") && !name.equals("minecraft:skull") && !name.equals("skull"))
        {
            return input;
        }
        final String facing = input.getCompound("Properties").getString("facing");
        final boolean wall = facing.equals("north") || facing.equals("south") || facing.equals("west") || facing.equals("east");
        final CompoundTag result = new CompoundTag();
        result.putString("Name", wall ? "minecraft:skeleton_wall_skull" : "minecraft:skeleton_skull");
        final CompoundTag properties = new CompoundTag();
        properties.putString(wall ? "facing" : "rotation", wall ? facing : "0");
        result.put("Properties", properties);
        return result;
    }

    static void fixMultiBlockStates(final SchematicConversionUtils result)
    {
        for (int y = 1; y < result.sizeY; y++)
        {
            for (int z = 0; z < result.sizeZ; z++)
            {
                for (int x = 0; x < result.sizeX; x++)
                {
                    if (!result.stateAt(x, y, z).getCompound("Properties").getString("half").equals("upper"))
                    {
                        continue;
                    }
                    final CompoundTag upper = result.stateAt(x, y, z).copy();
                    final CompoundTag properties = upper.getCompound("Properties");
                    final CompoundTag lower = result.stateAt(x, y - 1, z).copy();
                    if (upper.getString("Name").startsWith("minecraft:") && upper.getString("Name").endsWith("_door")
                        && upper.getString("Name").equals(lower.getString("Name")))
                    {
                        final CompoundTag lowerProperties = lower.getCompound("Properties");
                        properties.putString("facing", lowerProperties.getString("facing"));
                        properties.putString("open", lowerProperties.getString("open"));
                        lowerProperties.putString("hinge", properties.getString("hinge"));
                        lowerProperties.putString("powered", properties.getString("powered"));
                        result.setBlock(x, y - 1, z, result.addState(lower, SchematicConversionUtils.currentVersion()));
                    }
                    else if (isTallPlant(lower.getString("Name")) && isTallPlant(upper.getString("Name")))
                    {
                        upper.putString("Name", lower.getString("Name"));
                    }
                    else
                    {
                        continue;
                    }
                    result.setBlock(x, y, z, result.addState(upper, SchematicConversionUtils.currentVersion()));
                }
            }
        }
    }

    private static boolean isTallPlant(final String name)
    {
        return switch (name)
        {
            case "minecraft:sunflower", "minecraft:lilac", "minecraft:tall_grass", "minecraft:large_fern", "minecraft:rose_bush", "minecraft:peony" -> true;
            default -> false;
        };
    }

    /**
     * Some pre-flattening block properties lived in tile NBT instead of the numeric metadata.
     * Reuse Minecraft's own conversion tables before updating the remaining block entity data.
     */
    static boolean fixBlockEntityState(final SchematicConversionUtils result, final CompoundTag tile, final int[] pos)
    {
        final CompoundTag state = result.stateAt(pos[0], pos[1], pos[2]).copy();
        final String name = state.getString("Name");
        if (!name.startsWith("minecraft:"))
        {
            return false;
        }
        final CompoundTag properties = state.getCompound("Properties");
        Dynamic<?> replacement = null;
        boolean consumed = false;
        if (name.equals("minecraft:note_block"))
        {
            replacement = ChunkPalettedStorageFix.NOTE_BLOCK_MAP.get(Boolean.toString(tile.getBoolean("powered")) + Math.clamp(tile.getInt("note"), 0, 24));
            consumed = true;
        }
        else if (name.equals("minecraft:flower_pot") || name.startsWith("minecraft:potted_"))
        {
            String item = tile.getString("Item");
            if (tile.contains("Item", Tag.TAG_ANY_NUMERIC))
            {
                item = switch (tile.getInt("Item"))
                {
                    case 6 -> "sapling";
                    case 31 -> "tallgrass";
                    case 32 -> "deadbush";
                    case 37 -> "yellow_flower";
                    case 38 -> "red_flower";
                    case 39 -> "brown_mushroom";
                    case 40 -> "red_mushroom";
                    case 81 -> "cactus";
                    default -> "air";
                };
            }
            if (!item.contains(":"))
            {
                item = "minecraft:" + item;
            }
            replacement = ChunkPalettedStorageFix.FLOWER_POT_MAP.getOrDefault(item + tile.getInt("Data"), ChunkPalettedStorageFix.FLOWER_POT_MAP.get("minecraft:air0"));
            consumed = true;
        }
        else if (name.endsWith("_bed"))
        {
            state.putString("Name", "minecraft:" + DyeColor.byId(tile.contains("color") ? tile.getInt("color") : 14).getName() + "_bed");
            result.setBlock(pos[0], pos[1], pos[2], result.addState(state, SchematicConversionUtils.currentVersion()));
        }
        else if (name.endsWith("_skull") || name.endsWith("_head"))
        {
            final boolean wall = name.contains("_wall_");
            String skullName = switch (tile.getInt("SkullType"))
            {
                case 1 -> "wither_skeleton_skull";
                case 2 -> "zombie_head";
                case 3 -> "player_head";
                case 4 -> "creeper_head";
                case 5 -> "dragon_head";
                default -> "skeleton_skull";
            };
            if (wall)
            {
                skullName = skullName.replace("_skull", "_wall_skull").replace("_head", "_wall_head");
            }
            else
            {
                properties.putString("rotation", Integer.toString(tile.getInt("Rot") & 15));
            }
            state.putString("Name", "minecraft:" + skullName);
            result.setBlock(pos[0], pos[1], pos[2], result.addState(state, SchematicConversionUtils.currentVersion()));
        }
        else if (name.endsWith("_banner"))
        {
            final String color = DyeColor.byId(15 - (tile.contains("Base") ? tile.getInt("Base") : 15)).getName();
            state.putString("Name", "minecraft:" + color + (name.contains("_wall_") ? "_wall_banner" : "_banner"));
            result.setBlock(pos[0], pos[1], pos[2], result.addState(state, SchematicConversionUtils.currentVersion()));
        }
        if (replacement != null)
        {
            result.setBlock(pos[0], pos[1], pos[2], result.addState(asTag(replacement), FLATTENED_VERSION));
        }
        return consumed;
    }

    private static CompoundTag asTag(final Dynamic<?> dynamic)
    {
        return (CompoundTag) dynamic.convert(NbtOps.INSTANCE).getValue();
    }
}
