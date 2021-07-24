package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.Structurize;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

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

    private static void update(@NotNull final File input, @NotNull final File globalInputFolder, @NotNull final File globalOutputFolder)
    {
        if (input.isDirectory())
        {
            for (final File file : input.listFiles())
            {
                update(file, globalInputFolder, globalOutputFolder);
            }
            return;
        }

        if (input.getPath().endsWith(".blueprint"))
        {
            return;
        }

        try
        {
            final File output = new File(globalOutputFolder, input.toString().replaceAll("\\.nbt", ".blueprint").replace(globalInputFolder.toString(), ""));
            output.getParentFile().mkdirs();

            CompoundTag blueprint = NbtIo.readCompressed(Files.newInputStream(input.toPath()));
            if (blueprint == null || blueprint.isEmpty())
            {
                return;
            }

            // blueprint = StructureUtils.getFixer().process(FixTypes.STRUCTURE, blueprint);
            // TODO: this! (datafixer)

            final ListTag blocks = blueprint.getList("blocks", NBT.TAG_COMPOUND);
            final ListTag pallete = blueprint.getList("palette", NBT.TAG_COMPOUND);

            final CompoundTag bluePrintCompound = new CompoundTag();

            final ListTag list = blueprint.getList("size", NBT.TAG_INT);
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
                final ListTag entities = blueprint.getList("entities", NBT.TAG_COMPOUND);
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

    private static void updatePos(final MutableBlockPos pos, final CompoundTag comp)
    {
        final ListTag list = comp.getList("pos", NBT.TAG_INT);
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