package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.ldtteam.structurize.update.DomumOrnamentumUpdateHandler;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.util.Tuple;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;
import static com.ldtteam.structurize.blueprints.v1.BlueprintUtil.*;

/**
 * Command to update all schematics in structurize/updater/input to the blueprint format to structurize/updater/output.
 */
public class UpdateSchematicsCommand extends AbstractCommand
{
    private final static String NAME = "updateschematics";

    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(NAME).executes(s -> onExecute(s));
    }

    private static int onExecute(final CommandContext<CommandSourceStack> command) throws CommandSyntaxException
    {
        final File updaterInput = new File(Structurize.proxy.getSchematicsFolder(), "/updater/input");
        final File updaterOutput = new File(Structurize.proxy.getSchematicsFolder(), "/updater/output");

        updaterInput.mkdirs();

        for (final File file : updaterInput.listFiles())
        {
            update(file, updaterInput, updaterOutput);
        }
        return 1;
    }

    private static void update(final File input, final File globalInputFolder, final File globalOutputFolder)
    {
        if (input.isDirectory())
        {
            for (final File file : input.listFiles())
            {
                update(file, globalInputFolder, globalOutputFolder);
            }
            return;
        }

        try
        {

            final File output = new File(globalOutputFolder, input.toString().replaceAll("\\.nbt", ".blueprint").replace(globalInputFolder.toString(), ""));
            output.getParentFile().mkdirs();

            if (input.getPath().endsWith(".blueprint"))
            {
                final Blueprint bluePrintCompound = fixBluePrints(input);

                if (bluePrintCompound != null)
                {
                    output.createNewFile();
                    NbtIo.writeCompressed(BlueprintUtil.writeBlueprintToNBT(bluePrintCompound), Files.newOutputStream(output.toPath()));
                }
                return;
            }

            CompoundTag blueprint = NbtIo.readCompressed(Files.newInputStream(input.toPath()));
            if (blueprint == null || blueprint.isEmpty())
            {
                return;
            }

            final ListTag blocks = blueprint.getList("blocks", Tag.TAG_COMPOUND);
            final ListTag pallete = blueprint.getList("palette", Tag.TAG_COMPOUND);

            final CompoundTag bluePrintCompound = new CompoundTag();

            final ListTag list = blueprint.getList("size", Tag.TAG_INT);
            final int[] size = new int[] {list.getInt(0), list.getInt(1), list.getInt(2)};
            bluePrintCompound.putShort("size_x", (short) size[0]);
            bluePrintCompound.putShort("size_y", (short) size[1]);
            bluePrintCompound.putShort("size_z", (short) size[2]);

            final boolean addStructureVoid = blocks.size() != size[0] * size[1] * size[2];
            short structureVoidID = 0;
            if (addStructureVoid)
            {
                structureVoidID = (short) pallete.size();
                pallete.add(NbtUtils.writeBlockState(Blocks.STRUCTURE_VOID.defaultBlockState()));
            }

            final Set<String> mods = new HashSet<>();

            for (int i = 0; i < pallete.size(); i++)
            {
                final CompoundTag blockState = pallete.getCompound(i);
                final String modid = blockState.getString("Name").split(":")[0];
                mods.add(modid);
            }

            final ListTag requiredMods = new ListTag();
            for (final String str : mods)
            {
                requiredMods.add(StringTag.valueOf(str));
            }

            bluePrintCompound.put("palette", pallete);
            bluePrintCompound.put("required_mods", requiredMods);

            final MutableBlockPos pos = new MutableBlockPos();
            final short[][][] dataArray = new short[size[1]][size[2]][size[0]];

            if (addStructureVoid)
            {
                for (int i = 0; i < size[1]; i++)
                {
                    for (int j = 0; j < size[2]; j++)
                    {
                        for (int k = 0; k < size[0]; k++)
                        {
                            dataArray[i][j][k] = structureVoidID;
                        }
                    }
                }
            }

            final ListTag tileEntities = new ListTag();
            for (int i = 0; i < blocks.size(); i++)
            {
                final CompoundTag comp = blocks.getCompound(i);
                updatePos(pos, comp);
                dataArray[pos.getY()][pos.getZ()][pos.getX()] = (short) comp.getInt("state");
                if (comp.contains("nbt"))
                {
                    final CompoundTag te = comp.getCompound("nbt");
                    te.putShort("x", (short) pos.getX());
                    te.putShort("y", (short) pos.getY());
                    te.putShort("z", (short) pos.getZ());
                    tileEntities.add(te);
                }
            }

            bluePrintCompound.putIntArray("blocks", convertBlocksToSaveData(dataArray, (short) size[0], (short) size[1], (short) size[2]));
            bluePrintCompound.put("tile_entities", tileEntities);
            bluePrintCompound.put("architects", new ListTag());
            bluePrintCompound.put("name", (StringTag.valueOf(input.getName().replaceAll("\\.nbt", ""))));
            bluePrintCompound.putInt("version", 1);

            final ListTag newEntities = new ListTag();
            if (blueprint.contains("entities"))
            {
                final ListTag entities = blueprint.getList("entities", Tag.TAG_COMPOUND);
                for (int i = 0; i < entities.size(); i++)
                {
                    final CompoundTag entityData = entities.getCompound(i);
                    final CompoundTag entity = entityData.getCompound("nbt");
                    entity.put("Pos", entityData.get("pos"));
                    newEntities.add(entity);
                }
            }
            bluePrintCompound.put("entities", newEntities);

            output.createNewFile();
            NbtIo.writeCompressed(bluePrintCompound, Files.newOutputStream(output.toPath()));
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static Blueprint fixBluePrints(final File input)
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(input);
            byte[] data = StructureLoadingUtils.getStreamAsByteArray(inputStream);
            inputStream.close();
            final CompoundTag compoundNBT = NbtIo.readCompressed(new ByteArrayInputStream(data));

            return readBlueprintFromNBT(compoundNBT);
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Could not read file:" + input.getName());
        }
        return null;
    }

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
            List<BlockState> palette = new ArrayList<>();

            // Reading Blocks
            short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks"), sizeX, sizeY, sizeZ);

            // Reading Tile Entities
            CompoundTag[] tes = fixTileEntities(oldDataVersion, (ListTag) tag.get("tile_entities"));

            final List<CompoundTag> teList = new ArrayList<>();

            UpdateSchematicsCommand.fixPalette(oldDataVersion, palette, teList, paletteTag, blocks, new BlockPos(sizeX, sizeY, sizeZ));

            teList.addAll(Arrays.stream(tes).toList());

            final CompoundTag[] tileEntities = teList.toArray(new CompoundTag[0]);


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

    public static void fixPalette(
      final int oldDataVersion,
      final List<BlockState> palette,
      final List<CompoundTag> tileEntities,
      final ListTag paletteTag, final short[][][] blocks, final BlockPos blockPos)
    {
        final short paletteSize = (short) paletteTag.size();

        for (short i = 0; i < paletteSize; i++)
        {
            final CompoundTag nbt = paletteTag.getCompound(i);
            try
            {
                final CompoundTag fixedNbt = DataFixerUtils.runDataFixer(nbt, References.BLOCK_STATE, oldDataVersion);
                final String name = fixedNbt.getString("Name");
                if (!name.startsWith("%s:".formatted(MOD_ID)))
                {
                    final BlockState state = NbtUtils.readBlockState(fixedNbt);
                    palette.add(i, state);
                    continue;
                }

                final Optional<Tuple<BlockState, Optional<BlockEntity>>> replacementData = DomumOrnamentumUpdateHandler.createBlockReplacementData(fixedNbt);
                if (replacementData.isEmpty())
                {
                    final BlockState state = NbtUtils.readBlockState(fixedNbt);
                    palette.add(i, state);
                    continue;
                }

                palette.add(i, replacementData.get().getA());
                if (replacementData.get().getB().isPresent())
                {
                    for (int y = 0; y < blockPos.getY(); y++)
                    {
                        for (int x = 0; x < blockPos.getX(); x++)
                        {
                            for (int z = 0; z < blockPos.getZ(); z++)
                            {
                                if (blocks[y][z][x] == i)
                                {
                                    final CompoundTag targetTag = replacementData.get().getB().get().serializeNBT().copy();
                                    targetTag.putInt("x", x);
                                    targetTag.putInt("y", y);
                                    targetTag.putInt("z", z);

                                    tileEntities.add(targetTag);
                                }
                            }
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                palette.add(i, Blocks.AIR.defaultBlockState());
                Log.getLogger().warn("Blueprint reader: something went wrong loading block at position: " + i, e);
            }
        }
    }

    private static void updatePos(final MutableBlockPos pos, final CompoundTag comp)
    {
        final ListTag list = comp.getList("pos", Tag.TAG_INT);
        pos.set(list.getInt(0), list.getInt(1), list.getInt(2));
    }

    /**
     * Converts a 3 Dimensional short Array to a one Dimensional int Array.
     *
     * @param multDimArray 3 Dimensional short Array
     * @param sizeX        Sturcture size on the X-Axis
     * @param sizeY        Sturcture size on the Y-Axis
     * @param sizeZ        Sturcture size on the Z-Axis
     * @return An 1 Dimensional int array
     */
    private static int[] convertBlocksToSaveData(final short[][][] multDimArray, final short sizeX, final short sizeY, final short sizeZ)
    {
        // Converting 3 Dimensional Array to One DImensional
        final short[] oneDimArray = new short[sizeX * sizeY * sizeZ];

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
        final int[] ints = new int[(int) Math.ceil(oneDimArray.length / 2f)];

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
}