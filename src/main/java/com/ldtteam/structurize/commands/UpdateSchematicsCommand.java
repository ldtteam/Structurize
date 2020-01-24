package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.Structurize;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.BlockPos.Mutable;
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

    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral(NAME).executes(s -> onExecute(s));
    }

    private static int onExecute(final CommandContext<CommandSource> command) throws CommandSyntaxException
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

            CompoundNBT blueprint = CompressedStreamTools.readCompressed(Files.newInputStream(input.toPath()));
            if (blueprint == null || blueprint.isEmpty())
            {
                return;
            }

            // blueprint = StructureUtils.getFixer().process(FixTypes.STRUCTURE, blueprint);
            // TODO: this! (datafixer)

            final ListNBT blocks = blueprint.getList("blocks", NBT.TAG_COMPOUND);
            final ListNBT pallete = blueprint.getList("palette", NBT.TAG_COMPOUND);

            final CompoundNBT bluePrintCompound = new CompoundNBT();

            final ListNBT list = blueprint.getList("size", NBT.TAG_INT);
            final int[] size = new int[] {list.getInt(0), list.getInt(1), list.getInt(2)};
            bluePrintCompound.putShort("size_x", (short) size[0]);
            bluePrintCompound.putShort("size_y", (short) size[1]);
            bluePrintCompound.putShort("size_z", (short) size[2]);

            final boolean addStructureVoid = blocks.size() != size[0] * size[1] * size[2];
            short structureVoidID = 0;
            if (addStructureVoid)
            {
                structureVoidID = (short) pallete.size();
                pallete.add(NBTUtil.writeBlockState(Blocks.STRUCTURE_VOID.getDefaultState()));
            }

            final Set<String> mods = new HashSet<>();

            for (int i = 0; i < pallete.size(); i++)
            {
                final CompoundNBT blockState = pallete.getCompound(i);
                final String modid = blockState.getString("Name").split(":")[0];
                mods.add(modid);
            }

            final ListNBT requiredMods = new ListNBT();
            for (final String str : mods)
            {
                requiredMods.add(StringNBT.valueOf(str));
            }

            bluePrintCompound.put("palette", pallete);
            bluePrintCompound.put("required_mods", requiredMods);

            final Mutable pos = new Mutable();
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

            final ListNBT tileEntities = new ListNBT();
            for (int i = 0; i < blocks.size(); i++)
            {
                final CompoundNBT comp = blocks.getCompound(i);
                updatePos(pos, comp);
                dataArray[pos.getY()][pos.getZ()][pos.getX()] = (short) comp.getInt("state");
                if (comp.contains("nbt"))
                {
                    final CompoundNBT te = comp.getCompound("nbt");
                    te.putShort("x", (short) pos.getX());
                    te.putShort("y", (short) pos.getY());
                    te.putShort("z", (short) pos.getZ());
                    tileEntities.add(te);
                }
            }

            bluePrintCompound.putIntArray("blocks", convertBlocksToSaveData(dataArray, (short) size[0], (short) size[1], (short) size[2]));
            bluePrintCompound.put("tile_entities", tileEntities);
            bluePrintCompound.put("architects", new ListNBT());
            bluePrintCompound.put("name", (StringNBT.valueOf(input.getName().replaceAll("\\.nbt", ""))));
            bluePrintCompound.putInt("version", 1);

            final ListNBT newEntities = new ListNBT();
            if (blueprint.contains("entities"))
            {
                final ListNBT entities = blueprint.getList("entities", NBT.TAG_COMPOUND);
                for (int i = 0; i < entities.size(); i++)
                {
                    final CompoundNBT entityData = entities.getCompound(i);
                    final CompoundNBT entity = entityData.getCompound("nbt");
                    entity.put("Pos", entityData.get("pos"));
                    newEntities.add(entity);
                }
            }
            bluePrintCompound.put("entities", newEntities);

            output.createNewFile();
            CompressedStreamTools.writeCompressed(bluePrintCompound, Files.newOutputStream(output.toPath()));
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void updatePos(final Mutable pos, final CompoundNBT comp)
    {
        final ListNBT list = comp.getList("pos", NBT.TAG_INT);
        pos.setPos(list.getInt(0), list.getInt(1), list.getInt(2));
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