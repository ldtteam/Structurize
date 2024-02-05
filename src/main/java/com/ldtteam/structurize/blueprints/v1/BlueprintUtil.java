package com.ldtteam.structurize.blueprints.v1;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.*;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

/**
 * @see <a href="http://dark-roleplay.net/other/blueprint_format.php">Blueprint V1 Specification</a>
 * @since 0.1.0 State: not completed
 */
public class BlueprintUtil
{
    public static final int DEFAULT_FIXER_IF_NOT_FOUND = DataVersion.v1_12_2.getDataVersion();

    public static final String NBT_OPTIONAL_DATA_TAG = "optional_data";

    /**
     * Generates a Blueprint objects from the world
     *
     * @param world The World that is used for the Blueprint
     * @param pos   The Position of the Blueprint
     * @param sizeX The Size on the X-Axis
     * @param sizeY The Size on the Y-Axis
     * @param sizeZ The Size on the Z-Axis
     * @param name  a Name for the Structure
     * @return the generated Blueprint
     */
    public static Blueprint createBlueprint(
      Level world,
      BlockPos pos,
      final boolean saveEntities,
      short sizeX,
      short sizeY,
      short sizeZ,
      String name,
      Optional<BlockPos> anchorPos)
    {
        final List<BlockState> pallete = new ArrayList<>();
        // Allways add AIR to Pallete
        pallete.add(Blocks.AIR.defaultBlockState());
        final short[][][] structure = new short[sizeY][sizeZ][sizeX];
        final List<CompoundTag> tileEntities = new ArrayList<>();

        final List<String> requiredMods = new ArrayList<>();

        for (final BlockPos mutablePos : BlockPos.betweenClosed(pos, pos.offset(sizeX - 1, sizeY - 1, sizeZ - 1)))
        {
            BlockState state = world.getBlockState(mutablePos);
            String modName = ForgeRegistries.BLOCKS.getKey(state.getBlock()).getNamespace();

            short x = (short) (mutablePos.getX() - pos.getX()), y = (short) (mutablePos.getY() - pos.getY()),
              z = (short) (mutablePos.getZ() - pos.getZ());

            if (!modName.equals("minecraft") && !modName.equals(MOD_ID))
            {
                if (!ModList.get().getModContainerById(modName).isPresent())
                {
                    structure[y][z][x] = (short) pallete.indexOf(Blocks.AIR.defaultBlockState());
                    continue;
                }
                if (!requiredMods.contains(modName))
                {
                    requiredMods.add(modName);
                }
            }

            final LevelChunk chunk = world.getChunkAt(mutablePos);
            final BlockEntity te = chunk.getBlockEntities().containsKey(mutablePos) && !chunk.getBlockEntities().get(mutablePos).isRemoved() ? chunk.getBlockEntity(mutablePos) : world.getBlockEntity(mutablePos.immutable());
            if (te != null)
            {
                CompoundTag teTag = te.saveWithFullMetadata();
                teTag.putShort("x", x);
                teTag.putShort("y", y);
                teTag.putShort("z", z);
                tileEntities.add(teTag);
            }
            if (!pallete.contains(state))
            {
                pallete.add(state);
            }
            structure[y][z][x] = (short) pallete.indexOf(state);
        }

        final CompoundTag[] tes = tileEntities.toArray(new CompoundTag[0]);

        final List<CompoundTag> entitiesTag = new ArrayList<>();

        List<Entity> entities = new ArrayList<>();
        if (saveEntities)
        {
            entities = world.getEntities(null,
              new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + sizeX, pos.getY() + sizeY, pos.getZ() + sizeZ));
        }

        for (final Entity entity : entities)
        {
            if (!entity.getType().canSerialize())
            {
                continue;
            }

            final Vec3 oldPos = entity.position();
            final CompoundTag entityTag = entity.serializeNBT();

            final ListTag posList = new ListTag();
            posList.add(DoubleTag.valueOf(oldPos.x - pos.getX()));
            posList.add(DoubleTag.valueOf(oldPos.y - pos.getY()));
            posList.add(DoubleTag.valueOf(oldPos.z - pos.getZ()));

            BlockPos entityPos = entity.blockPosition();
            if (entity instanceof HangingEntity)
            {
                entityPos = ((HangingEntity) entity).getPos();
            }
            entityTag.put("Pos", posList);
            entityTag.put("TileX", IntTag.valueOf(entityPos.getX() - pos.getX()));
            entityTag.put("TileY", IntTag.valueOf(entityPos.getY() - pos.getY()));
            entityTag.put("TileZ", IntTag.valueOf(entityPos.getZ() - pos.getZ()));
            entitiesTag.add(entityTag);
        }

        final Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, (short) pallete.size(), pallete, structure, tes, requiredMods);
        schem.setEntities(entitiesTag.toArray(new CompoundTag[0]));

        if (anchorPos.isPresent())
        {
            BlockPos relativeAnchorPos = new BlockPos(anchorPos.get().subtract(pos));

            schem.setCachePrimaryOffset(relativeAnchorPos);
        }

        // Blueprints do auto-calc anchors when missing, so if it uses a blueprint provider as anchor we fill in the schematic data afterwards to both TE and blueprint
        final BlockEntity tile = world.getBlockEntity(pos.offset(schem.getPrimaryBlockOffset()));
        if (tile instanceof IBlueprintDataProviderBE)
        {
            final CompoundTag blueprintData = (CompoundTag) schem.getBlockInfoAsMap().get(schem.getPrimaryBlockOffset()).getTileEntityData().get(TAG_BLUEPRINTDATA);

            if (name != null)
            {
                final String fileName = FilenameUtils.getBaseName(name);
                blueprintData.putString(TAG_SCHEMATIC_NAME, fileName);
                ((IBlueprintDataProviderBE) tile).setSchematicName(fileName);
                ((IBlueprintDataProviderBE) tile).setBlueprintPath(name);
            }

            final BlockPos corner1 = BlockPos.ZERO.subtract(schem.getPrimaryBlockOffset());
            final BlockPos corner2 = new BlockPos(sizeX - 1, sizeY - 1, sizeZ - 1).subtract(schem.getPrimaryBlockOffset());
            ((IBlueprintDataProviderBE) tile).setSchematicCorners(corner1, corner2);
            BlockPosUtil.writeToNBT(blueprintData, TAG_CORNER_ONE, corner1);
            BlockPosUtil.writeToNBT(blueprintData, TAG_CORNER_TWO, corner2);

            if (!world.isClientSide)
            {
                ((ServerLevel) world).getChunkSource().blockChanged(pos);
            }
        }

        if (name != null)
        {
            schem.setName(name);
        }

        return schem;
    }

    /**
     * Serializes a given Blueprint to an CompoundNBT
     *
     * @param schem The Blueprint to serialize
     * @return An CompoundNBT containing the Blueprint Data
     */
    public static CompoundTag writeBlueprintToNBT(final Blueprint schem)
    {
        final CompoundTag tag = new CompoundTag();
        // Set Blueprint Version
        tag.putByte("version", (byte) 1);
        // Set Blueprint Size
        tag.putShort("size_x", schem.getSizeX());
        tag.putShort("size_y", schem.getSizeY());
        tag.putShort("size_z", schem.getSizeZ());

        // Create Pallete
        final BlockState[] palette = schem.getPalette();
        final ListTag paletteTag = new ListTag();
        for (short i = 0; i < schem.getPalleteSize(); i++)
        {
            paletteTag.add(NbtUtils.writeBlockState(palette[i]));
        }
        tag.put("palette", paletteTag);

        // Adding blocks
        final int[] blockInt = convertBlocksToSaveData(schem.getStructure(), schem.getSizeX(), schem.getSizeY(), schem.getSizeZ());
        tag.putIntArray("blocks", blockInt);

        // Adding Tile Entities
        final ListTag finishedTes = new ListTag();
        final CompoundTag[] tes = Arrays.stream(schem.getTileEntities())
                                    .flatMap(Arrays::stream)
                                    .flatMap(Arrays::stream)
                                    .filter(Objects::nonNull)
                                    .toArray(CompoundTag[]::new);
        finishedTes.addAll(Arrays.asList(tes));
        tag.put("tile_entities", finishedTes);

        // Adding Entities
        final ListTag finishedEntities = new ListTag();
        final CompoundTag[] entities = schem.getEntities();
        finishedEntities.addAll(Arrays.asList(entities));
        tag.put("entities", finishedEntities);

        // Adding Required Mods
        final List<String> requiredMods = schem.getRequiredMods();
        final ListTag modsList = new ListTag();
        for (String requiredMod : requiredMods)
        {
            // modsList.set(i,);
            modsList.add(StringTag.valueOf(requiredMod));
        }
        tag.put("required_mods", modsList);

        final String name = schem.getName();
        final String[] architects = schem.getArchitects();

        if (name != null)
        {
            tag.putString("name", name);
        }
        if (architects != null)
        {
            final ListTag architectsTag = new ListTag();
            for (final String architect : architects)
            {
                architectsTag.add(StringTag.valueOf(architect));
            }
            tag.put("architects", architectsTag);
        }

        tag.put("mcversion", IntTag.valueOf(SharedConstants.getCurrentVersion().getDataVersion().getVersion()));

        final CompoundTag optionalTag = new CompoundTag();
        final CompoundTag structurizeTag = new CompoundTag();

        BlockPosUtil.writeToNBT(structurizeTag, "primary_offset", schem.getPrimaryBlockOffset());

        optionalTag.put(MOD_ID, structurizeTag);
        tag.put(NBT_OPTIONAL_DATA_TAG, optionalTag);

        return tag;
    }

    public static List<BlockState> fixPalette(final int oldDataVersion, final ListTag paletteTag)
    {
        final short paletteSize = (short) paletteTag.size();
        final List<BlockState> palette = new ArrayList<>();

        for (short i = 0; i < paletteSize; i++)
        {
            final CompoundTag nbt = paletteTag.getCompound(i);
            try
            {
                final CompoundTag fixedNbt = DataFixerUtils.runDataFixer(nbt, References.BLOCK_STATE, oldDataVersion);

                switch (oldDataVersion)
                {
                    case 1343:
                        fixPalette1343(fixedNbt);
                    default:
                        // don't fix anything
                        break;
                }

                final BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), fixedNbt);
                palette.add(i, state);
            }
            catch (final Exception e)
            {
                palette.add(i, Blocks.AIR.defaultBlockState());
                Log.getLogger().warn("Blueprint reader: something went wrong loading block at position: " + i, e);
            }
        }

        return palette;
    }

    private static void fixPalette1343(final CompoundTag oldBlockState)
    {
        final String name = oldBlockState.getString("Name");
        oldBlockState.putString("Name", oldBlockState.getString("Name").toLowerCase(Locale.US));
        if (name.contains(MOD_ID))
        {
            if (name.contains("blockshingle_"))
            {
                final String[] split = name.split(":")[1].split("_");
                oldBlockState.putString("Name",
                  "structurize:clay_" + (split.length > 2 ? split[1] + "_" + split[2] : split[1]) + "_shingle");
            }
            else if (name.contains("blockshingleslab"))
            {
                oldBlockState.putString("Name", "structurize:clay_shingle_slab");
            }
            else if (name.contains("blocktimberframe"))
            {
                final String[] split = name.split(":")[1].split("_");
                String output = "structurize:" + (split.length > 3 ? split[3] : split[2]) + "_"
                                  + (split.length > 3 ? split[1] + "_" + split[2] : split[1]) + "_paper_timber_frame";
                output = output.replace("doublecrossed", "double_crossed");
                output = output.replace("sideframed", "side_framed");
                output = output.replace("upgated", "up_gated");
                output = output.replace("downgated", "down_gated");
                output = output.replace("onecrossedlr", "one_crossed_lr");
                output = output.replace("onecrossedrl", "one_crossed_rl");
                output = output.replace("horizontalplain", "horizontal_plain");
                output = output.replace("sideframedhorizontal", "side_framed_horizontal");

                oldBlockState.putString("Name", output);
                // blocktimberframe_spruce_plain
                // plain_spruce_paper_timber_frame
            }
            else if (name.contains("blockpaperwall") && !name.contains("_"))
            {
                oldBlockState.putString("Name",
                  "structurize:" + oldBlockState.getCompound("Properties").getString("variant") + "_blockpaperwall");
            }
        }
    }

    public static CompoundTag[] fixTileEntities(final int oldDataVersion, final ListTag tileEntitiesTag)
    {
        final CompoundTag[] tileEntities = new CompoundTag[tileEntitiesTag.size()];

        for (short i = 0; i < tileEntities.length; i++)
        {
            final CompoundTag nbt = tileEntitiesTag.getCompound(i);

            try
            {
                final String id = nbt.getString("id");

                if (id.contains("minecolonies"))
                {
                    nbt.putString("id", id.toLowerCase(Locale.US));
                    nbt.putString("Item", nbt.getString("Item".toLowerCase(Locale.US)));
                    tileEntities[i] = nbt;
                    continue;
                }
                // no longer a block entity, fixed in #fixCross1343()
                if (id.equals("minecraft:flower_pot") || id.equals("minecraft:noteblock"))
                {
                    tileEntities[i] = nbt;
                    continue;
                }

                tileEntities[i] = id.startsWith("minecraft:")
                                    ? DataFixerUtils.runDataFixer(nbt, References.BLOCK_ENTITY, oldDataVersion)
                                    : nbt;
            }
            catch (Exception e)
            {
                tileEntities[i] = null;
                Log.getLogger().warn("Blueprint reader: something went wrong loading tile entity at position: " + i, e);
            }
        }

        return tileEntities;
    }

    public static CompoundTag[] fixEntities(final int oldDataVersion, final ListTag entitiesTag)
    {
        final CompoundTag[] entities = new CompoundTag[entitiesTag.size()];

        for (short i = 0; i < entities.length; i++)
        {
            final CompoundTag nbt = entitiesTag.getCompound(i);

            try
            {
                final String id = nbt.getString("id");

                entities[i] = id.startsWith("minecraft:") ? DataFixerUtils.runDataFixer(nbt, References.ENTITY, oldDataVersion) : nbt;
            }
            catch (Exception e)
            {
                entities[i] = null;
                Log.getLogger().warn("Blueprint reader: something went wrong loading entity at position: " + i, e);
            }
        }

        return entities;
    }

    private static List<BlockPos> searchForBlockIdInBlocks(final short idToCheck, final short[][][] blocks)
    {
        final List<BlockPos> result = new ArrayList<>();
        for (short y = 0; y < blocks.length; y++)
        {
            final short[][] temp = blocks[y];
            for (short z = 0; z < temp.length; z++)
            {
                final short[] temp2 = temp[z];
                for (short x = 0; x < temp2.length; x++)
                {
                    final short id = temp2[x];
                    if (id == idToCheck)
                    {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return result;
    }

    private static Map<Integer, BlockPos> searchForTEposInTEs(final List<BlockPos> blockPosToFind, final CompoundTag[] tileEntities)
    {
        final Map<Integer, BlockPos> result = new HashMap<>();
        for (int i = 0; i < tileEntities.length; i++)
        {
            final CompoundTag compound = tileEntities[i];
            if (compound != null)
            {
                final BlockPos bp = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
                if (blockPosToFind.contains(bp))
                {
                    result.put(i, bp);
                }
            }
        }
        return result;
    }

    private static void teToBlockStateFix(
      final List<BlockState> palette,
      final short[][][] blocks,
      final CompoundTag[] tileEntities,
      final short paletteIndex,
      final Function<CompoundTag, CompoundTag> dataFixer)
    {
        final Map<Integer, BlockPos> teToReplace = searchForTEposInTEs(searchForBlockIdInBlocks(paletteIndex, blocks), tileEntities);
        final Map<BlockState, Short> newBlocksToBlockId = new HashMap<>();
        boolean paletteFull = false;

        palette.set(paletteIndex, null);
        for (final Map.Entry<Integer, BlockPos> e : teToReplace.entrySet())
        {
            final CompoundTag teCompound = tileEntities[e.getKey()];
            tileEntities[e.getKey()] = null;
            final CompoundTag newBScompound = dataFixer.apply(teCompound);
            final BlockState newBlockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), newBScompound);
            final short newBlockId = paletteFull ? newBlocksToBlockId.getOrDefault(newBlockState, (short) palette.size()) : paletteIndex;
            if (newBlockId == palette.size())
            {
                palette.add(newBlockId, newBlockState);
                newBlocksToBlockId.put(newBlockState, newBlockId);
            }
            else if (!paletteFull)
            {
                palette.set(newBlockId, newBlockState);
                newBlocksToBlockId.put(newBlockState, newBlockId);
                paletteFull = true;
            }
            blocks[e.getValue().getY()][e.getValue().getZ()][e.getValue().getX()] = newBlockId;
        }
    }

    public static void fixCross1343(
      final List<BlockState> palette,
      final short[][][] blocks,
      final CompoundTag[] tileEntities,
      final CompoundTag[] entities)
    {
        final int oldSize = palette.size();
        for (short i = 0; i < oldSize; i++)
        {
            final BlockState bs = palette.get(i);
            if (bs.getBlock() == Blocks.POTTED_CACTUS) // flower pot fix
            {
                teToBlockStateFix(palette, blocks, tileEntities, i, teCompound -> {
                    final String type = teCompound.getString("Item") + teCompound.getInt("Data");
                    return (CompoundTag) ChunkPalettedStorageFix.FLOWER_POT_MAP
                                           .getOrDefault(type, ChunkPalettedStorageFix.FLOWER_POT_MAP.get("minecraft:air0"))
                                           .getValue();
                });
            }
            else if (bs.getBlock() == Blocks.NOTE_BLOCK) // note block fix
            {
                teToBlockStateFix(palette, blocks, tileEntities, i, teCompound -> {
                    final String type = Boolean.toString(teCompound.getBoolean("powered"))
                                          + (byte) Math.min(Math.max(teCompound.getInt("note"), 0), 24);
                    return (CompoundTag) ChunkPalettedStorageFix.NOTE_BLOCK_MAP
                                           .getOrDefault(type, ChunkPalettedStorageFix.NOTE_BLOCK_MAP.get("false0"))
                                           .getValue();
                });
            }
        }
    }

    /**
     * Deserializes a Blueprint form the Given CompoundNBT
     *
     * @param nbtTag The CompoundNBT containing the Blueprint Data
     * @return A desserialized Blueprint
     */
    public static Blueprint readBlueprintFromNBT(final CompoundTag nbtTag)
    {
        final CompoundTag tag = nbtTag;
        byte version = tag.getByte("version");
        if (version == 1)
        {
            short sizeX = tag.getShort("size_x"), sizeY = tag.getShort("size_y"), sizeZ = tag.getShort("size_z");

            // Reading required Mods
            List<String> requiredMods = new ArrayList<>();
            List<String> missingMods = new ArrayList<>();
            ListTag modsList = (ListTag) tag.get("required_mods");
            short modListSize = (short) modsList.size();
            for (int i = 0; i < modListSize; i++)
            {
                requiredMods.add((modsList.get(i)).getAsString());
                if (!requiredMods.get(i).equals("minecraft") && !ModList.get().getModContainerById(requiredMods.get(i)).isPresent())
                {
                    LogManager.getLogger().warn("Found missing mods for Blueprint, some blocks may be missing: " + requiredMods.get(i));
                    missingMods.add(requiredMods.get(i));
                }
            }

            final int oldDataVersion = tag.contains("mcversion") ? tag.getInt("mcversion") : DEFAULT_FIXER_IF_NOT_FOUND;

            // Reading Pallete
            ListTag paletteTag = (ListTag) tag.get("palette");
            List<BlockState> palette = fixPalette(oldDataVersion, paletteTag);

            // Reading Blocks
            short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks"), sizeX, sizeY, sizeZ);

            // Reading Tile Entities
            CompoundTag[] tileEntities = fixTileEntities(oldDataVersion, (ListTag) tag.get("tile_entities"));

            // Reading Entities
            CompoundTag[] entities = fixEntities(oldDataVersion, (ListTag) tag.get("entities"));

            if (oldDataVersion == DEFAULT_FIXER_IF_NOT_FOUND)
            {
                fixCross1343(palette, blocks, tileEntities, entities);
            }

            final Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, (short) palette.size(), palette, blocks, tileEntities, requiredMods)
                                      .setMissingMods(missingMods.toArray(new String[0]));

            schem.setEntities(entities);

            if (tag.getAllKeys().contains("name"))
            {
                schem.setName(tag.getString("name"));
            }
            if (tag.getAllKeys().contains("architects"))
            {
                ListTag architectsTag = (ListTag) tag.get("architects");
                String[] architects = new String[architectsTag.size()];
                for (int i = 0; i < architectsTag.size(); i++)
                {
                    architects[i] = architectsTag.getString(i);
                }
                schem.setArchitects(architects);
            }

            if (tag.getAllKeys().contains(NBT_OPTIONAL_DATA_TAG))
            {
                final CompoundTag optionalTag = tag.getCompound(NBT_OPTIONAL_DATA_TAG);
                if (optionalTag.getAllKeys().contains(MOD_ID))
                {
                    final CompoundTag structurizeTag = optionalTag.getCompound(MOD_ID);
                    BlockPos offsetPos = BlockPosUtil.readFromNBT(structurizeTag, "primary_offset");
                    schem.setCachePrimaryOffset(offsetPos);
                }
            }

            return schem;
        }
        return null;
    }

    /**
     * Attempts to write a Blueprint to an Output Stream
     *
     * @param os    The Output Stream to write to
     * @param schem The Blueprint to write
     */
    public static void writeToStream(OutputStream os, Blueprint schem)
    {
        try
        {
            NbtIo.writeCompressed(writeBlueprintToNBT(schem), os);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Converts a 3 Dimensional short Array to a one Dimensional int Array
     *
     * @param multDimArray 3 Dimensional short Array
     * @param sizeX        Sturcture size on the X-Axis
     * @param sizeY        Sturcture size on the Y-Axis
     * @param sizeZ        Sturcture size on the Z-Axis
     * @return An 1 Dimensional int array
     */
    private static int[] convertBlocksToSaveData(short[][][] multDimArray, short sizeX, short sizeY, short sizeZ)
    {
        // Converting 3 Dimensional Array to One DImensional
        short[] oneDimArray = new short[sizeX * sizeY * sizeZ];

        int j = 0;
        for (short y = 0; y < sizeY; y++)
        {
            for (short z = 0; z < sizeZ; z++)
            {
                for (short x = 0; x < sizeX; x++)
                {
                    oneDimArray[j++] = multDimArray[y][z][x];
                }
            }
        }

        // Converting short Array to int Array
        int[] ints = new int[(int) Math.ceil(oneDimArray.length / 2f)];

        int currentInt;
        for (int i = 1; i < oneDimArray.length; i += 2)
        {
            currentInt = oneDimArray[i - 1];
            currentInt = currentInt << 16 | oneDimArray[i];
            ints[(int) Math.ceil(i / 2f) - 1] = currentInt;
        }
        if (oneDimArray.length % 2 == 1)
        {
            currentInt = oneDimArray[oneDimArray.length - 1] << 16;
            ints[ints.length - 1] = currentInt;
        }
        return ints;
    }

    /**
     * Converts a 1 Dimensional int Array to a 3 Dimensional short Array
     *
     * @param ints  1 Dimensioanl int Array
     * @param sizeX Sturcture size on the X-Axis
     * @param sizeY Sturcture size on the Y-Axis
     * @param sizeZ Sturcture size on the Z-Axis
     * @return An 3 Dimensional short array
     */
    public static short[][][] convertSaveDataToBlocks(int[] ints, short sizeX, short sizeY, short sizeZ)
    {
        // Convert int array to short array
        short[] oneDimArray = new short[ints.length * 2];

        for (int i = 0; i < ints.length; i++)
        {
            oneDimArray[i * 2] = (short) (ints[i] >> 16);
            oneDimArray[(i * 2) + 1] = (short) (ints[i]);
        }

        // Convert 1 Dimensional Array to 3 Dimensional Array
        short[][][] multDimArray = new short[sizeY][sizeZ][sizeX];

        int i = 0;
        for (short y = 0; y < sizeY; y++)
        {
            for (short z = 0; z < sizeZ; z++)
            {
                for (short x = 0; x < sizeX; x++)
                {
                    multDimArray[y][z][x] = oneDimArray[i++];
                }
            }
        }
        return multDimArray;
    }
}
