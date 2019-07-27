package com.ldtteam.structures.blueprints.v1;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraft.util.datafix.fixes.BlockNameFlattening;
import net.minecraft.util.datafix.fixes.BlockStateFlattenStructures;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import net.minecraft.util.datafix.fixes.BlockStateFlatternEntities;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    public static Blueprint createBlueprint(World world, BlockPos pos, short sizeX, short sizeY, short sizeZ, String name, String... architects)
    {
        final List<BlockState> pallete = new ArrayList<>();
        //Allways add AIR to Pallete
        pallete.add(Blocks.AIR.getDefaultState());
        final short[][][] structure = new short[sizeY][sizeZ][sizeX];
        final List<CompoundNBT> tileEntities = new ArrayList<>();

        final List<String> requiredMods = new ArrayList<>();

        for (final BlockPos mutablePos : BlockPos.getAllInBoxMutable(pos, pos.add(sizeX - 1, sizeY - 1, sizeZ - 1)))
        {
            BlockState state = world.getBlockState(mutablePos);
            String modName = state.getBlockState().getBlock().getRegistryName().getNamespace();

            short x = (short) (mutablePos.getX() - pos.getX()), y = (short) (mutablePos.getY() - pos.getY()), z = (short) (mutablePos.getZ() - pos.getZ());

            if (!modName.equals("minecraft"))
            {
                if (!requiredMods.contains(modName))
                {
                    if (!ModList.get().getModContainerById(modName).isPresent())
                    {
                        requiredMods.add(modName);
                    }
                }
                else if (!ModList.get().getModContainerById(modName).isPresent())
                {
                    structure[y][z][x] = (short) pallete.indexOf(Blocks.AIR.getDefaultState());
                    continue;
                }
            }

            final TileEntity te = world.getTileEntity(mutablePos);
            if (te != null)
            {
                CompoundNBT teTag = te.serializeNBT();
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

        final CompoundNBT[] tes = tileEntities.toArray(new CompoundNBT[0]);

        final List<CompoundNBT> entitiesTag = new ArrayList<>();

        final List<Entity> entities =
          world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + sizeX, pos.getY() + sizeY, pos.getZ() + sizeZ));

        for (final Entity entity : entities)
        {
            final Vec3d oldPos = entity.getPositionVector();
            final CompoundNBT entityTag = new CompoundNBT();
            entity.deserializeNBT(entityTag);

            final ListNBT posList = new ListNBT();
            posList.add(new DoubleNBT(oldPos.x - pos.getX()));
            posList.add(new DoubleNBT(oldPos.y - pos.getY()));
            posList.add(new DoubleNBT(oldPos.z - pos.getZ()));

            BlockPos entityPos = entity.getPosition();
            if (entity instanceof HangingEntity)
            {
                entityPos = ((HangingEntity) entity).getHangingPosition();
            }
            entityTag.put("Pos", posList);
            entityTag.put("TileX", new IntNBT(entityPos.getX() - pos.getX()));
            entityTag.put("TileY", new IntNBT(entityPos.getY() - pos.getY()));
            entityTag.put("TileZ", new IntNBT(entityPos.getZ() - pos.getZ()));
            entitiesTag.add(entityTag);
        }

        final Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, (short) pallete.size(), pallete, structure, tes,
          requiredMods);
        schem.setEntities(entitiesTag.toArray(new CompoundNBT[0]));

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
     * Serializes a given Blueprint to an CompoundNBT
     *
     * @param schem The Blueprint to serialize
     * @return An CompoundNBT containing the Blueprint Data
     */
    public static CompoundNBT writeBlueprintToNBT(final Blueprint schem)
    {
        final CompoundNBT tag = new CompoundNBT();
        // Set Blueprint Version
        tag.putByte("version", (byte) 1);
        // Set Blueprint Size
        tag.putShort("size_x", schem.getSizeX());
        tag.putShort("size_y", schem.getSizeY());
        tag.putShort("size_z", schem.getSizeZ());

        // Create Pallete
        final BlockState[] palette = schem.getPalette();
        final ListNBT paletteTag = new ListNBT();
        for (short i = 0; i < schem.getPalleteSize(); i++)
        {
            paletteTag.add(NBTUtil.writeBlockState(palette[i].getBlockState()));
        }
        tag.put("palette", paletteTag);

        // Adding blocks
        final int[] blockInt = convertBlocksToSaveData(schem.getStructure(), schem.getSizeX(), schem.getSizeY(),
          schem.getSizeZ());
        tag.putIntArray("blocks", blockInt);

        // Adding Tile Entities
        final ListNBT finishedTes = new ListNBT();
        final CompoundNBT[] tes = Arrays.stream(schem.getTileEntities()).flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).toArray(CompoundNBT[]::new);
        finishedTes.addAll(Arrays.asList(tes));
        tag.put("tile_entities", finishedTes);

        // Adding Entities
        final ListNBT finishedEntities = new ListNBT();
        final CompoundNBT[] entities = schem.getEntities();
        finishedEntities.addAll(Arrays.asList(entities));
        tag.put("entities", finishedEntities);

        // Adding Required Mods
        final List<String> requiredMods = schem.getRequiredMods();
        final ListNBT modsList = new ListNBT();
        for (String requiredMod : requiredMods)
        {
            // modsList.set(i,);
            modsList.add(new StringNBT(requiredMod));
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
            final ListNBT architectsTag = new ListNBT();
            for (final String architect : architects)
            {
                architectsTag.add(new StringNBT(architect));
            }
            tag.put("architects", architectsTag);
        }

        return tag;
    }

    /**
     * Deserializes a Blueprint form the Given CompoundNBT
     *
     * @param nbtTag The CompoundNBT containing the Blueprint Data
     * @param fixer  the data fixer.
     * @return A desserialized Blueprint
     */
    public static Blueprint readBlueprintFromNBT(final CompoundNBT nbtTag, final DataFixer fixer)
    {
        final CompoundNBT tag = nbtTag;
        byte version = tag.getByte("version");
        if (version == 1)
        {
            short sizeX = tag.getShort("size_x"), sizeY = tag.getShort("size_y"), sizeZ = tag.getShort("size_z");

            // Reading required Mods
            List<String> requiredMods = new ArrayList<>();
            List<String> missingMods = new ArrayList<>();
            ListNBT modsList = (ListNBT) tag.get("required_mods");
            short modListSize = (short) modsList.size();
            for (int i = 0; i < modListSize; i++)
            {
                requiredMods.add((modsList.get(i)).getString());
                if (!requiredMods.get(i).equals("minecraft") && !ModList.get().getModContainerById(requiredMods.get(i)).isPresent())
                {
                    LogManager.getLogger().warn(
                      "Found missing mods for Blueprint, some blocks may be missing: " + requiredMods.get(i));
                    missingMods.add(requiredMods.get(i));
                }
            }

            // Reading Pallete
            ListNBT paletteTag = (ListNBT) tag.get("palette");
            //paletteTag = NBTUtil.update(fixer, DefaultTypeReferences.CHUNK, paletteTag, 1945);

            final Schema schema = DataFixesManager.getDataFixer().getSchema(1450);
            short paletteSize = (short) paletteTag.size();
            List<BlockState> palette = new ArrayList<>();
            for (short i = 0; i < paletteSize; i++)
            {
                palette.add(i, NBTUtil.readBlockState((CompoundNBT) BlockStateFlatteningMap.updateNBT(new Dynamic<>(NBTDynamicOps.INSTANCE, paletteTag.getCompound(i))).getValue()));
            }

            // Reading Blocks
            short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks"), sizeX, sizeY, sizeZ);

            // Reading Tile Entities
            ListNBT teTag = (ListNBT) tag.get("tile_entities");
            CompoundNBT[] tileEntities = new CompoundNBT[teTag.size()];
            for (short i = 0; i < tileEntities.length; i++)
            {
                tileEntities[i] = teTag.getCompound(i);
            }

            // Reading Entities
            ListNBT entitiesTag = (ListNBT) tag.get("entities");
            CompoundNBT[] entities = new CompoundNBT[entitiesTag.size()];
            for (short i = 0; i < entities.length; i++)
            {
                entities[i] = entitiesTag.getCompound(i);
            }

            final Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, paletteSize, palette, blocks, tileEntities, requiredMods).setMissingMods(missingMods.toArray(new String[0]));

            schem.setEntities(entities);

            if (tag.keySet().contains("name"))
            {
                schem.setName(tag.getString("name"));
            }
            if (tag.keySet().contains("architects"))
            {
                ListNBT architectsTag = (ListNBT) tag.get("architects");
                String[] architects = new String[architectsTag.size()];
                for (int i = 0; i < architectsTag.size(); i++)
                {
                    architects[i] = architectsTag.getString(i);
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
    private static short[][][] convertSaveDataToBlocks(int[] ints, short sizeX, short sizeY, short sizeZ)
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
