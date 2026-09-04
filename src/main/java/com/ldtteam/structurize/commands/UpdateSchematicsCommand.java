package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static com.ldtteam.structurize.api.util.constant.Constants.*;
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
        final Path gameFolder = new File(".").toPath().resolve(BLUEPRINT_FOLDER).resolve(UPDATE_FOLDER);
        try
        {
            Files.createDirectories(gameFolder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            try (final Stream<Path> paths = Files.list(gameFolder.resolve("input")))
            {
                paths.forEach(element -> update(element, gameFolder.resolve("input"), gameFolder.resolve("output")));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        return 1;
    }

    private static void update(final Path input, final Path globalInputFolder, final Path globalOutputFolder)
    {
        if (Files.isDirectory(input))
        {
            try
            {
                try (final Stream<Path> paths = Files.list(input))
                {
                    paths.forEach(element -> update(element, globalInputFolder, globalOutputFolder));
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return;
        }

        try
        {

            final Path output = globalOutputFolder.resolve(input.toString().replaceAll("\\.nbt", ".blueprint").replace(globalInputFolder.toString(), ""));
            Files.createDirectories(output.getParent());

            if (input.toString().endsWith(".blueprint"))
            {
                final CompoundTag bluePrintCompound = writeBlueprintToNBT(fixBluePrints(input));
                try (final OutputStream outputstream = new BufferedOutputStream(Files.newOutputStream(output)))
                {
                    NbtIo.writeCompressed(bluePrintCompound, outputstream);
                }
                catch (final IOException e)
                {
                    Log.getLogger().warn("Exception while trying to scan.", e);
                }

                return;
            }

            CompoundTag blueprint = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(input)), NbtAccounter.unlimitedHeap());
            if (blueprint == null || blueprint.isEmpty())
            {
                return;
            }

            final ListTag blocks = blueprint.getListOrEmpty("blocks");
            final ListTag pallete = blueprint.getListOrEmpty("palette");

            final CompoundTag bluePrintCompound = new CompoundTag();

            final ListTag list = blueprint.getListOrEmpty("size");
            final int[] size = new int[] {
                list.getInt(0).orElse(0),
                list.getInt(1).orElse(0),
                list.getInt(2).orElse(0)
            };
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
                final CompoundTag blockState = pallete.getCompound(i).orElse(new CompoundTag());
                final String modid = blockState.getStringOr("Name", "").split(":")[0];
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
                final CompoundTag comp = blocks.getCompound(i).orElse(new CompoundTag());
                updatePos(pos, comp);
                dataArray[pos.getY()][pos.getZ()][pos.getX()] = (short) comp.getIntOr("state", 0);
                if (comp.contains("nbt"))
                {
                    final CompoundTag te = comp.getCompoundOrEmpty("nbt");
                    te.putShort("x", (short) pos.getX());
                    te.putShort("y", (short) pos.getY());
                    te.putShort("z", (short) pos.getZ());
                    tileEntities.add(te);
                }
            }

            bluePrintCompound.putIntArray("blocks", convertBlocksToSaveData(dataArray, (short) size[0], (short) size[1], (short) size[2]));
            bluePrintCompound.put("tile_entities", tileEntities);
            bluePrintCompound.put("architects", new ListTag());
            bluePrintCompound.put("name", (StringTag.valueOf(input.toString().replaceAll("\\.nbt", ""))));
            bluePrintCompound.putInt("version", 1);

            final ListTag newEntities = new ListTag();
            if (blueprint.contains("entities"))
            {
                final ListTag entities = blueprint.getListOrEmpty("entities");
                for (int i = 0; i < entities.size(); i++)
                {
                    final CompoundTag entityData = entities.getCompound(i).orElse(new CompoundTag());
                    final CompoundTag entity = entityData.getCompoundOrEmpty("nbt");
                    entity.put("Pos", entityData.get("pos"));
                    newEntities.add(entity);
                }
            }
            bluePrintCompound.put("entities", newEntities);

            try (final OutputStream outputstream = new BufferedOutputStream(Files.newOutputStream(output)))
            {
                NbtIo.writeCompressed(bluePrintCompound, outputstream);
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("Exception while trying to scan.", e);
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static Blueprint fixBluePrints(final Path input)
    {
        try
        {
            final CompoundTag compoundNBT = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(input)), NbtAccounter.unlimitedHeap());
            return readBlueprintFromNBT(compoundNBT);
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Could not read file:" + input.toString());
        }
        return null;
    }

    public static Blueprint readBlueprintFromNBT(final CompoundTag nbtTag)
    {
        final CompoundTag tag = nbtTag;
        byte version = tag.getByteOr("version", (byte) 0);
        if (version == 1)
        {
            short sizeX = tag.getShortOr("size_x", (short) 0), sizeY = tag.getShortOr("size_y", (short) 0), sizeZ = tag.getShortOr("size_z", (short) 0);

            // Reading required Mods
            List<String> requiredMods = new ArrayList<>();
            List<String> missingMods = new ArrayList<>();
            ListTag modsList = (ListTag) tag.get("required_mods");
            short modListSize = (short) modsList.size();
            for (int i = 0; i < modListSize; i++)
            {
                requiredMods.add(modsList.getStringOr(i, ""));
                if (!requiredMods.get(i).equals("minecraft") && !ModList.get().getModContainerById(requiredMods.get(i)).isPresent())
                {
                    LogManager.getLogger().warn("Found missing mods for Blueprint, some blocks may be missing: " + requiredMods.get(i));
                    missingMods.add(requiredMods.get(i));
                }
            }

            final int oldDataVersion = tag.contains("mcversion") ? tag.getIntOr("mcversion", 0) : DEFAULT_FIXER_IF_NOT_FOUND;

            // Reading Pallete
            ListTag paletteTag = (ListTag) tag.get("palette");
            List<BlockState> palette = new ArrayList<>();

            // Reading Blocks
            short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks").orElse(new int[0]), sizeX, sizeY, sizeZ);

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

            if (tag.keySet().contains("name"))
            {
                schem.setName(tag.getStringOr("name", ""));
            }
            if (tag.keySet().contains("architects"))
            {
                ListTag architectsTag = (ListTag) tag.get("architects");
                String[] architects = new String[architectsTag.size()];
                for (int i = 0; i < architectsTag.size(); i++)
                {
                    architects[i] = architectsTag.getString(i).orElse("");
                }
                schem.setArchitects(architects);
            }

            if (tag.keySet().contains(NBT_OPTIONAL_DATA_TAG))
            {
                final CompoundTag optionalTag = tag.getCompound(NBT_OPTIONAL_DATA_TAG).orElse(new CompoundTag());
                if (optionalTag.keySet().contains(MOD_ID))
                {
                    final CompoundTag structurizeTag = optionalTag.getCompound(MOD_ID).orElse(new CompoundTag());
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
            final CompoundTag nbt = paletteTag.getCompound(i).orElse(new CompoundTag());
            try
            {
                final CompoundTag fixedNbt = DataFixerUtils.runDataFixer(nbt, References.BLOCK_STATE, oldDataVersion);
                final String name = fixedNbt.getStringOr("Name", "");
                if (!name.startsWith("%s:".formatted(MOD_ID)))
                {
                    final BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, fixedNbt);
                    palette.add(i, state);
                    continue;
                }

                final BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, fixedNbt);
                palette.add(i, state);
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
        final ListTag list = comp.getListOrEmpty("pos");
        pos.set(
            list.getInt(0).orElse(0),
            list.getInt(1).orElse(0),
            list.getInt(2).orElse(0));
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
