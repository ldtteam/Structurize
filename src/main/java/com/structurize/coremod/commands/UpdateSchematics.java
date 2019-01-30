package com.structurize.coremod.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import com.google.common.collect.Lists;
import com.structurize.coremod.Structurize;
import com.structurize.structures.helpers.Structure;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.NotNull;

/**
 * Command to update all schematics in structurize/updater/input to the blueprint format to structurize/updater/output.
 */
public class UpdateSchematics implements ICommand
{
    @Override
    public int compareTo(@NotNull final ICommand o)
    {
        return 0;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "updateschematics";
    }

    @NotNull
    @Override
    public List<String> getAliases()
    {
        return Lists.newArrayList();
    }

    @NotNull
    @Override
    public String getUsage(@NotNull final ICommandSender sender)
    {
        return "/updateschematics";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args) throws CommandException
    {
        File updaterInput = new File(Structurize.proxy.getSchematicsFolder(), "/updater/input");
        File updaterOutput = new File(Structurize.proxy.getSchematicsFolder(), "/updater/output");

        updaterInput.mkdirs();

        for (final File file : updaterInput.listFiles())
        {
            this.update(file, updaterInput, updaterOutput);
        }
    }

    private void update(@NotNull final File input, @NotNull final File globalInputFolder, @NotNull final File globalOutputFolder)
    {
        if (input.isDirectory())
        {
            for (final File file : input.listFiles())
            {
                this.update(file, globalInputFolder, globalOutputFolder);
            }
            return;
        }

        if (input.getPath().endsWith(".blueprint"))
        {
            return;
        }

        try
        {
            File output = new File(globalOutputFolder, input.toString().replaceAll("\\.nbt", ".blueprint").replace(globalInputFolder.toString(), ""));
            output.getParentFile().mkdirs();

            NBTTagCompound template = CompressedStreamTools.readCompressed(Files.newInputStream(input.toPath()));
            if (template == null || template.isEmpty())
            {
                return;
            }

            template = Structure.getFixer().process(FixTypes.STRUCTURE, template);

            final NBTTagList blocks = template.getTagList("blocks", NBT.TAG_COMPOUND);
            final NBTTagList pallete = template.getTagList("palette", NBT.TAG_COMPOUND);

            final NBTTagCompound blueprint = new NBTTagCompound();

            final NBTTagList list = template.getTagList("size", NBT.TAG_INT);
            final int[] size = new int[] {list.getIntAt(0), list.getIntAt(1), list.getIntAt(2)};
            blueprint.setShort("size_x", (short) size[0]);
            blueprint.setShort("size_y", (short) size[1]);
            blueprint.setShort("size_z", (short) size[2]);

            final boolean addStructureVoid = blocks.tagCount() != size[0] * size[1] * size[2];
            short structureVoidID = 0;
            if (addStructureVoid)
            {
                structureVoidID = (short) pallete.tagCount();
                pallete.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), Blocks.STRUCTURE_VOID.getDefaultState()));
            }


            final Set<String> mods = new HashSet<String>();
            for (int i = 0; i < pallete.tagCount(); i++)
            {
                NBTTagCompound blockState = (NBTTagCompound) pallete.get(i);
                String modid = blockState.getString("Name").split(":")[0];
                mods.add(modid);
            }

            final NBTTagList requiredMods = new NBTTagList();
            for (String str : mods)
            {
                requiredMods.appendTag(new NBTTagString(str));
            }

            blueprint.setTag("palette", pallete);
            blueprint.setTag("required_mods", requiredMods);

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

            final NBTTagList tileEntities = new NBTTagList();
            for (int i = 0; i < blocks.tagCount(); i++)
            {
                final NBTTagCompound comp = blocks.getCompoundTagAt(i);
                updatePos(pos, comp);
                dataArray[pos.getY()][pos.getZ()][pos.getX()] = (short) comp.getInteger("state");
                if (comp.hasKey("nbt"))
                {
                    NBTTagCompound te = (NBTTagCompound) comp.getTag("nbt");
                    te.setShort("x", (short) pos.getX());
                    te.setShort("y", (short) pos.getY());
                    te.setShort("z", (short) pos.getZ());
                    tileEntities.appendTag(te);
                }
            }

            blueprint.setIntArray("blocks", convertBlocksToSaveData(dataArray, (short) size[0], (short) size[1], (short) size[2]));
            blueprint.setTag("tile_entities", tileEntities);
            blueprint.setTag("architects", new NBTTagList());
            blueprint.setTag("name", new NBTTagString(input.getName().replaceAll("\\.nbt", "")));
            blueprint.setInteger("version", 1);

            final NBTTagList newEntities = new NBTTagList();
            if (template.hasKey("entities"))
            {
                final NBTTagList entities = template.getTagList("entities", NBT.TAG_COMPOUND);
                for (int i = 0; i < entities.tagCount(); i++)
                {
                    NBTTagCompound entityData = entities.getCompoundTagAt(i);
                    NBTTagCompound entity = entityData.getCompoundTag("nbt");
                    entity.setTag("Pos", entityData.getTag("pos"));
                    newEntities.appendTag(entity);
                }
            }
            blueprint.setTag("entities", newEntities);

            output.createNewFile();
            CompressedStreamTools.writeCompressed(blueprint, Files.newOutputStream(output.toPath()));
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void updatePos(final MutableBlockPos pos ,final NBTTagCompound comp)
    {
        final NBTTagList list = comp.getTagList("pos", NBT.TAG_INT);
        pos.setPos(list.getIntAt(0), list.getIntAt(1), list.getIntAt(2));
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

    @Override
    public boolean checkPermission(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender)
    {
        return true;
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args, final BlockPos targetPos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
