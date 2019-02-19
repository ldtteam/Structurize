package com.ldtteam.structures.blueprints.v1;

import java.util.ArrayList;
import java.util.List;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

/**
 * The blueprint class which contains the file format for the schematics.
 */
public class Blueprint
{
    /**
     * The list of required mods.
     */
    private final List<String> requiredMods;

    /**
     * The size of the blueprint.
     */
    private short sizeX, sizeY, sizeZ;

    /**
     * The size of the pallete.
     */
    private short palleteSize;

    /**
     * The palette of different blocks.
     */
    private List<IBlockState> palette;

    /**
     * The name of the blueprint.
     */
    private String name;

    /**
     * The name of the builders.
     */
    private String[] architects;

    /**
     * A list of missing modids that were missing while this schematic was loaded
     */
    private String[] missingMods;

    /**
     * The Schematic Data, each short represents an entry in the {@link Blueprint#palette}
     */
    private short[][][] structure;

    /**
     * The tileentities.
     */
    private NBTTagCompound[][][] tileEntities;

    /**
     * The entities.
     */
    private NBTTagCompound[][][] entities;

    /**
     * Constructor of a new Blueprint.
     *
     * @param sizeX        the x size.
     * @param sizeY        the y size.
     * @param sizeZ        the z size.
     * @param palleteSize  the size of the pallete.
     * @param pallete      the palette.
     * @param structure    the structure data.
     * @param tileEntities the tileEntities.
     * @param requiredMods the required mods.
     */
    protected Blueprint(
      short sizeX,
      short sizeY,
      short sizeZ,
      short palleteSize,
      List<IBlockState> pallete,
      short[][][] structure,
      NBTTagCompound[] tileEntities,
      List<String> requiredMods)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palleteSize = palleteSize;
        this.palette = pallete;
        this.structure = structure;
        this.tileEntities = new NBTTagCompound[sizeY][sizeZ][sizeX];

        for (final NBTTagCompound te : tileEntities)
        {
            if (te != null)
            {
                this.tileEntities[te.getShort("y")][te.getShort("z")][te.getShort("x")] = te;
            }
        }
        this.requiredMods = requiredMods;
    }

    /**
     * Constructor of a new Blueprint.
     *
     * @param sizeX        the x size.
     * @param sizeY        the y size.
     * @param sizeZ        the z size.
     */
    public Blueprint(short sizeX, short sizeY, short sizeZ)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.structure = new short[sizeY][sizeZ][sizeX];
        this.tileEntities = new NBTTagCompound[sizeY][sizeZ][sizeX];
        this.entities = new NBTTagCompound[sizeY][sizeZ][sizeX];

        this.requiredMods = new ArrayList<>();
        this.palette = new ArrayList<>();
        this.palette.add(0, Blocks.AIR.getDefaultState());
    }

    /**
     * @return the Size of the Structure on the X-Axis (without rotation and/or
     * mirroring)
     */
    public short getSizeX()
    {
        return this.sizeX;
    }

    /**
     * @return the Size of the Structure on the Y-Axis (without rotation and/or
     * mirroring)
     */
    public short getSizeY()
    {
        return this.sizeY;
    }

    /**
     * @return the Size of the Structure on the Z-Axis (without rotation and/or
     * mirroring)
     */
    public short getSizeZ()
    {
        return this.sizeZ;
    }

    /**
     * @return the amount of Blockstates within the pallete
     */
    public short getPalleteSize()
    {
        return this.palleteSize;
    }

    /**
     * @return the pallete (without rotation and/or mirroring)
     */
    public IBlockState[] getPalette()
    {
        return this.palette.toArray(new IBlockState[0]);
    }

    /**
     * Add a blockstate to the structure.
     * @param pos the position to add it to.
     * @param state the state to add.
     */
    public void addBlockState(final BlockPos pos, final IBlockState state)
    {
        int index = -1;
        for (int i = 0; i < this.palette.size(); i++)
        {
            if (this.palette.get(i).equals(state))
            {
                index = i;
                break;
            }
        }

        if (index == -1)
        {
            index = this.palleteSize + 1;
            this.palleteSize++;
            this.palette.add(state);
        }

        this.structure[pos.getY()][pos.getZ()][pos.getX()] = (short) index;
    }

    /**
     * @return the structure (without rotation and/or mirroring)
     * The Coordinate order is: y, z, x
     */
    public short[][][] getStructure()
    {
        return this.structure;
    }

    /**
     * @return an array of serialized TileEntities (posX, posY and posZ tags have
     * been localized to coordinates within the structure)
     */
    public NBTTagCompound[][][] getTileEntities()
    {
        return this.tileEntities;
    }

    /**
     * @return an array of serialized TileEntities (the Pos tag has
     * been localized to coordinates within the structure)
     */
    public NBTTagCompound[][][] getEntities()
    {
        return this.entities;
    }

    /**
     * @param entities an array of serialized TileEntities (the Pos tag need to
     *                 be localized to coordinates within the structure)
     */
    public void setEntities(NBTTagCompound[] entities)
    {
        this.entities = new NBTTagCompound[sizeY][sizeZ][sizeX];
        for (final NBTTagCompound te : entities)
        {
            if (te != null)
            {
                this.entities[te.getShort("y")][te.getShort("z")][te.getShort("x")] = te;
            }
        }
    }

    /**
     * @return a list of all required mods as modid's
     */
    public List<String> getRequiredMods()
    {
        return this.requiredMods;
    }

    /**
     * @return the Name of the Structure
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name of the Structure
     */
    public Blueprint setName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * @return an Array of all architects for this structure
     */
    public String[] getArchitects()
    {
        return this.architects;
    }

    /**
     * Sets an Array of all architects for this structure
     */
    public Blueprint setArchitects(String[] architects)
    {
        this.architects = architects;
        return this;
    }

    /**
     * @return An Array of all missing mods that are required to generate this
     * structure (only works if structure was loaded from file)
     */
    public String[] getMissingMods()
    {
        return this.missingMods;
    }

    /**
     * Sets the missing mods
     */
    public Blueprint setMissingMods(String... missingMods)
    {
        this.missingMods = missingMods;
        return this;
    }

    /**
     * Get a list of all entities in the blueprint as a list.
     * @return the list of nbttagcompounds.
     */
    public final List<Tuple<BlockPos, NBTTagCompound>> getEntitiesAsList()
    {
        final List<Tuple<BlockPos, NBTTagCompound>> list = new ArrayList<>();
        for (short x = 0; x < this.sizeX; x++)
        {
            for (short y = 0; y < this.sizeY; y++)
            {
                for (short z = 0; z < this.sizeZ; z++)
                {
                    list.add(new Tuple<>(new BlockPos(x,y,z), entities[y][z][x]));
                }
            }
        }
        return list;
    }

    /**
     * Get a list of all blockInfo objects in the blueprint.
     * @return a list of all blockinfo (position, blockState, tileEntityData).
     */
    public final List<BlockInfo> getBlockInfoAsList()
    {
        final List<BlockInfo> list = new ArrayList<>();
        for (short x = 0; x < this.sizeX; x++)
        {
            for (short y = 0; y < this.sizeY; y++)
            {
                for (short z = 0; z < this.sizeZ; z++)
                {
                    final BlockPos tempPos = new BlockPos(x, y, z);
                    final short value = structure[y][z][x];
                    final IBlockState state = palette.get(value & 0xFFFF);
                    list.add(new BlockInfo(tempPos, state, tileEntities[y][z][x]));
                }
            }
        }
        return list;
    }

    /**
     * Rotate the structure depending on the direction it's facing.
     *
     * @param rotation  times to rotateWithMirror.
     * @param pos the pos to rotateWithMirror it around.
     * @param mirror    the mirror.
     */
    public BlockPos rotateWithMirror(final Rotation rotation, final BlockPos pos, final Mirror mirror)
    {
        final BlockPos resultSize = transformedBlockPos(sizeX, sizeY, sizeZ, mirror, rotation);
        final short newSizeX = (short) resultSize.getX();
        final short newSizeY = (short) resultSize.getY();
        final short newSizeZ = (short) resultSize.getZ();

        final short[][][] newStructure = new short[newSizeY][newSizeZ][newSizeX];
        final NBTTagCompound[][][] newEntities = new NBTTagCompound[newSizeY][newSizeZ][newSizeX];
        final NBTTagCompound[][][] newTileEntities = new NBTTagCompound[newSizeY][newSizeZ][newSizeX];

        final List<IBlockState> palette = new ArrayList<>();
        for (int i = 0; i < this.palette.size(); i++)
        {
            palette.add(i, this.palette.get(i).withRotation(rotation).withMirror(mirror));
        }

        this.palette = palette;

        boolean foundAnchor = false;
        BlockPos offset = pos;

        for (short x = 0; x < this.sizeX; x++)
        {
            for (short y = 0; y < this.sizeY; y++)
            {
                for (short z = 0; z < this.sizeZ; z++)
                {
                    final BlockPos tempPos = transformedBlockPos(x, y, z, mirror, rotation);
                    final short value = structure[y][z][x];
                    final IBlockState state = palette.get(value & 0xFFFF);
                    if (state.getBlock() == Blocks.STRUCTURE_VOID)
                    {
                        continue;
                    }
                    if (state.getBlock() instanceof IAnchorBlock)
                    {
                        offset = tempPos;
                        foundAnchor = true;
                    }
                    newStructure[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = value;

                    final NBTTagCompound compound = tileEntities[y][z][x];
                    if (compound != null)
                    {
                        compound.setInteger("x", tempPos.getX());
                        compound.setInteger("y", tempPos.getY());
                        compound.setInteger("z", tempPos.getZ());
                    }
                    newTileEntities[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = compound;

                    final NBTTagCompound entitiesCompound = entities[y][z][x];
                    if (compound != null)
                    {
                        compound.setInteger("x", tempPos.getX());
                        compound.setInteger("y", tempPos.getY());
                        compound.setInteger("z", tempPos.getZ());
                    }
                    newEntities[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = entitiesCompound;
                }
            }
        }

        sizeX = newSizeX;
        sizeY = newSizeY;
        sizeZ = newSizeZ;


        this.structure = newStructure;
        this.entities = newEntities;
        this.tileEntities = newTileEntities;

        return foundAnchor ? offset : new BlockPos(resultSize.getX() / 2, 0, resultSize.getZ() / 2);
    }

    /**
     * Transforms a blockpos with mirror and rotation.
     *
     * @param xIn      the x input.
     * @param y        the y input.
     * @param zIn      the z input.
     * @param mirror   the mirror.
     * @param rotation the rotation.
     * @return the resulting position.
     */
    public static BlockPos transformedBlockPos(final int xIn, final int y, final int zIn, final Mirror mirror, final Rotation rotation)
    {
        int x = xIn;
        int z = zIn;

        boolean flag = true;

        switch (mirror)
        {
            case LEFT_RIGHT:
                z = -zIn;
                break;
            case FRONT_BACK:
                x = -xIn;
                break;
            default:
                flag = false;
        }

        switch (rotation)
        {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(z, y, -x);
            case CLOCKWISE_90:
                return new BlockPos(-z, y, x);
            case CLOCKWISE_180:
                return new BlockPos(-x, y, -z);
            default:
                return flag ? new BlockPos(x, y, z) : new BlockPos(x, y, z);
        }
    }
}
