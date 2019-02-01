package com.ldtteam.structures.blueprints.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.Template.EntityInfo;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.Loader;

/**
 * @see <a href="http://dark-roleplay.net/other/blueprint_format.php">Blueprint V1 Specification</a>
 * @since 0.1.0
 * State: not completed
 */
public class BlueprintUtil
{

    /**
     * Generates a Blueprint objects from the world
     *
     * @param world The World that is used for the Blueprint
     * @param pos   The Position of the Blueprint
     * @param sizeX The Size on the X-Axis
     * @param sizeY The Size on the Y-Axis
     * @param sizeZ The Size on the Z-Axis
     * @return the generated Blueprint
     */
    public static Blueprint createBlueprint(World world, BlockPos pos, short sizeX, short sizeY, short sizeZ)
    {
        return createBlueprint(world, pos, sizeX, sizeY, sizeZ, null);
    }

    /**
     * Generates a Blueprint objects from the world
     *
     * @param world      The World that is used for the Blueprint
     * @param pos        The Position of the Blueprint
     * @param sizeX      The Size on the X-Axis
     * @param sizeY      The Size on the Y-Axis
     * @param sizeZ      The Size on the Z-Axis
     * @param name       a Name for the Structure
     * @param architects an Array of Architects for the structure
     * @return the generated Blueprint
     */
    public static Blueprint createBlueprint(
      World world, BlockPos pos, short sizeX, short sizeY, short sizeZ,
      String name, String... architects)
    {
        List<IBlockState> pallete = new ArrayList<IBlockState>();
        //Allways add AIR to Pallete
        pallete.add(Blocks.AIR.getDefaultState());
        short[][][] structure = new short[sizeY][sizeZ][sizeX];

        List<NBTTagCompound> tileEntities = new ArrayList<NBTTagCompound>();

        List<String> requiredMods = new ArrayList<String>();

        Iterator<MutableBlockPos> positions = BlockPos.getAllInBoxMutable(pos, pos.add(sizeX - 1, sizeY - 1, sizeZ - 1)).iterator();
        while (positions.hasNext())
        {
            MutableBlockPos mutablePos = positions.next();
            IBlockState state = world.getBlockState(mutablePos);
            String modName = state.getBlock().getRegistryName().getNamespace();

            short x = (short) (mutablePos.getX() - pos.getX()), y = (short) (mutablePos.getY() - pos.getY()), z = (short) (mutablePos.getZ() - pos.getZ());

            if (!requiredMods.contains(modName))
            {
                if (Loader.isModLoaded(modName))
                {
                    requiredMods.add(modName);
                }
            }
            else if (!Loader.isModLoaded(modName))
            {
                structure[y][z][x] = (short) pallete.indexOf(Blocks.AIR.getDefaultState());
                continue;
            }

            TileEntity te = world.getTileEntity(mutablePos);
            if (te != null)
            {
                NBTTagCompound teTag = te.serializeNBT();
                teTag.setShort("x", x);
                teTag.setShort("y", y);
                teTag.setShort("z", z);
                tileEntities.add(teTag);
            }
            if (!pallete.contains(state))
            {
                pallete.add(state);
            }
            structure[y][z][x] = (short) pallete.indexOf(state);
        }

        IBlockState[] states = new IBlockState[pallete.size()];
        states = pallete.toArray(states);

        NBTTagCompound[] tes = new NBTTagCompound[tileEntities.size()];
        tes = tileEntities.toArray(tes);

        List<NBTTagCompound> entitiesTag = new ArrayList<NBTTagCompound>();

        List<Entity> entities =
          world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + sizeX, pos.getY() + sizeY, pos.getZ() + sizeZ));

        for (Entity entity : entities)
        {
            Vec3d oldPos = entity.getPositionVector();
            entity.setPosition(oldPos.x - pos.getX(), oldPos.y - pos.getY(), oldPos.z - pos.getZ());
            entitiesTag.add(entity.writeToNBT(new NBTTagCompound()));
            entity.setPosition(oldPos.x, oldPos.y, oldPos.z);
        }

        Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, (short) states.length, states, structure, tes,
          requiredMods);
        schem.setEntities(entitiesTag.toArray(new NBTTagCompound[entitiesTag.size()]));

        if (name != null)
        {
            schem.setName(name);
        }

        if (architects != null)
        {
            schem.setArchitects(architects);
        }

        return schem;
    }

    /**
     * Serializes a given Blueprint to an NBTTagCompound
     *
     * @param schem The Blueprint to serialize
     * @return An NBTTagCompound containing the Blueprint Data
     */
    public static NBTTagCompound writeBlueprintToNBT(Blueprint schem)
    {
        NBTTagCompound tag = new NBTTagCompound();
        // Set Blueprint Version
        tag.setByte("version", (byte) 1);
        // Set Blueprint Size
        tag.setShort("size_x", schem.getSizeX());
        tag.setShort("size_y", schem.getSizeY());
        tag.setShort("size_z", schem.getSizeZ());

        // Create Pallete
        IBlockState[] palette = schem.getPallete();
        NBTTagList paletteTag = new NBTTagList();
        for (short i = 0; i < schem.getPalleteSize(); i++)
        {
            NBTTagCompound state = new NBTTagCompound();
            NBTUtil.writeBlockState(state, palette[i]);
            paletteTag.appendTag(state);
        }
        tag.setTag("palette", paletteTag);

        // Adding blocks
        int[] blockInt = convertBlocksToSaveData(schem.getStructure(), schem.getSizeX(), schem.getSizeY(),
          schem.getSizeZ());
        tag.setIntArray("blocks", blockInt);

        // Adding Tile Entities
        NBTTagList finishedTes = new NBTTagList();
        NBTTagCompound[] tes = schem.getTileEntities();
        for (int i = 0; i < tes.length; i++)
        {
            finishedTes.appendTag(tes[i]);
        }
        tag.setTag("tile_entities", finishedTes);

        // Adding Entities
        NBTTagList finishedEntities = new NBTTagList();
        NBTTagCompound[] entities = schem.getEntities();
        for (int i = 0; i < entities.length; i++)
        {
            finishedEntities.appendTag(entities[i]);
        }
        tag.setTag("entities", finishedEntities);

        // Adding Required Mods
        List<String> requiredMods = schem.getRequiredMods();
        NBTTagList modsList = new NBTTagList();
        for (int i = 0; i < requiredMods.size(); i++)
        {
            // modsList.set(i,);
            modsList.appendTag(new NBTTagString(requiredMods.get(i)));
        }
        tag.setTag("required_mods", modsList);

        String name = schem.getName();
        String[] architects = schem.getArchitects();

        if (name != null)
        {
            tag.setString("name", name);
        }
        if (architects != null)
        {
            NBTTagList architectsTag = new NBTTagList();
            for (String architect : architects)
            {
                architectsTag.appendTag(new NBTTagString(architect));
            }
            tag.setTag("architects", architectsTag);
        }

        return tag;
    }

    /**
     * Deserializes a Blueprint form the Given NBTTagCompound
     *
     * @param nbtTag The NBTTagCompound containing the Blueprint Data
     * @param fixer the data fixer.
     * @return A desserialized Blueprint
     */
    public static Blueprint readBlueprintFromNBT(final NBTTagCompound nbtTag, final DataFixer fixer)
    {
        final NBTTagCompound tag = fixer.process(FixTypes.STRUCTURE, nbtTag);
        byte version = tag.getByte("version");
        if (version == 1)
        {
            short sizeX = tag.getShort("size_x"), sizeY = tag.getShort("size_y"), sizeZ = tag.getShort("size_z");

            // Reading required Mods
            List<String> requiredMods = new ArrayList<String>();
            List<String> missingMods = new ArrayList<String>();
            NBTTagList modsList = (NBTTagList) tag.getTag("required_mods");
            short modListSize = (short) modsList.tagCount();
            for (int i = 0; i < modListSize; i++)
            {
                requiredMods.add(((NBTTagString) modsList.get(i)).getString());
                if (!Loader.isModLoaded(requiredMods.get(i)))
                {
                    LogManager.getLogger().warn(
                      "Found missing mods for Blueprint, some blocks may be missing: " + requiredMods.get(i));
                    missingMods.add(requiredMods.get(i));
                }
            }

            // Reading Pallete
            NBTTagList paletteTag = (NBTTagList) tag.getTag("palette");
            short paletteSize = (short) paletteTag.tagCount();
            IBlockState[] palette = new IBlockState[paletteSize];
            for (short i = 0; i < palette.length; i++)
            {
                palette[i] = NBTUtil.readBlockState(paletteTag.getCompoundTagAt(i));
            }

            // Reading Blocks
            short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks"), sizeX, sizeY, sizeZ);

            // Reading Tile Entities
            NBTTagList teTag = (NBTTagList) tag.getTag("tile_entities");
            NBTTagCompound[] tileEntities = new NBTTagCompound[teTag.tagCount()];
            for (short i = 0; i < tileEntities.length; i++)
            {
                tileEntities[i] = teTag.getCompoundTagAt(i);
            }

            // Reading Entities
            NBTTagList entitiesTag = (NBTTagList) tag.getTag("entities");
            NBTTagCompound[] entities = new NBTTagCompound[entitiesTag.tagCount()];
            for (short i = 0; i < entities.length; i++)
            {
                entities[i] = entitiesTag.getCompoundTagAt(i);
            }

            Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, paletteSize, palette, blocks, tileEntities,
              requiredMods).setMissingMods(missingMods.toArray(new String[missingMods.size()]));

            schem.setEntities(entities);

            if (tag.hasKey("name"))
            {
                schem.setName(tag.getString("name"));
            }
            if (tag.hasKey("architects"))
            {
                NBTTagList architectsTag = (NBTTagList) tag.getTag("architects");
                String[] architects = new String[architectsTag.tagCount()];
                for (int i = 0; i < architectsTag.tagCount(); i++)
                {
                    architects[i] = architectsTag.getStringTagAt(i);
                }
                schem.setArchitects(architects);
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
            CompressedStreamTools.writeCompressed(writeBlueprintToNBT(schem), os);
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
    protected static int[] convertBlocksToSaveData(short[][][] multDimArray, short sizeX, short sizeY, short sizeZ)
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

        int currentInt = 0;
        for (int i = 1; i < oneDimArray.length; i += 2)
        {
            currentInt = oneDimArray[i - 1];
            currentInt = currentInt << 16 | oneDimArray[i];
            ints[(int) Math.ceil(i / 2f) - 1] = currentInt;
            currentInt = 0;
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
    protected static short[][][] convertSaveDataToBlocks(int[] ints, short sizeX, short sizeY, short sizeZ)
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

    /**
     * Converts a blueprint to a template.
     * @param bp the blueprint to convert.
     * @return a new template.
     */
    public static Template toTemplate(Blueprint bp)
    {
        Template temp = new Template();
        temp.size = new BlockPos(bp.getSizeX(), bp.getSizeY(), bp.getSizeZ());

        NBTTagCompound[] tes = bp.getTileEntities();

        Map<Long, NBTTagCompound> blockPosTes = new HashMap<Long, NBTTagCompound>();

        for (NBTTagCompound te : tes)
        {
            BlockPos pos = new BlockPos(te.getShort("x"), te.getShort("y"), te.getShort("z"));
            blockPosTes.put(pos.toLong(), te);
        }

        IBlockState[] pallete = bp.getPallete();
        short[][][] structure = bp.getStructure();
        for (int y = 0; y < bp.getSizeY(); y++)
        {
            for (int z = 0; z < bp.getSizeZ(); z++)
            {
                for (int x = 0; x < bp.getSizeX(); x++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    NBTTagCompound te = blockPosTes.get(pos.toLong());
                    temp.blocks.add(new Template.BlockInfo(pos, pallete[structure[y][z][x]], te));
                }
            }
        }

        NBTTagCompound[] entities = bp.getEntities();
        for (NBTTagCompound entity : entities)
        {
            NBTTagList posTag = entity.getTagList("Pos", NBT.TAG_DOUBLE);
            Vec3d vec = new Vec3d(posTag.getDoubleAt(0), posTag.getDoubleAt(1), posTag.getDoubleAt(2));
            BlockPos pos = new BlockPos(vec);
            temp.entities.add(new EntityInfo(vec, pos, entity));
        }

//		bp.

        return temp;
    }
}
