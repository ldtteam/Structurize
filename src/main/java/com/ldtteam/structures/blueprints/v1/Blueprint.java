package com.ldtteam.structures.blueprints.v1;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

/**
 * The blueprint class which contains the file format for the schematics.
 */
public class Blueprint
{
    /**
     * The list of required mods.
     */
    private List<String> requiredMods;

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
    private IBlockState[] palette;

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
    private NBTTagCompound[] tileEntities;

    /**
     * The entities.
     */
    private NBTTagCompound[] entities;

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
      short sizeX, short sizeY, short sizeZ, short palleteSize, IBlockState[] pallete,
      short[][][] structure, NBTTagCompound[] tileEntities, List<String> requiredMods)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palleteSize = palleteSize;
        this.palette = pallete;
        this.structure = structure;
        this.tileEntities = tileEntities;
        this.requiredMods = requiredMods;
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
    public IBlockState[] getPallete()
    {
        return this.palette;
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
    public NBTTagCompound[] getTileEntities()
    {
        return this.tileEntities;
    }

    /**
     * @return an array of serialized TileEntities (the Pos tag has
     * been localized to coordinates within the structure)
     */
    public NBTTagCompound[] getEntities()
    {
        return this.entities;
    }

    /**
     * @param entities an array of serialized TileEntities (the Pos tag need to
     *                 be localized to coordinates within the structure)
     */
    public void setEntities(NBTTagCompound[] entities)
    {
        this.entities = entities;
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
     * Transforms a BlockPos corresponding to a rotation and mirror value
     *
     * @param mirror   The value of mirroring
     * @param rotation The value to rotate the blockstate
     * @return a Tranformed BlockPos
     */
    protected static BlockPos transformedBlockPos(int x, int y, int z, Mirror mirror, Rotation rotation)
    {
        boolean flag = true;

        switch (mirror)
        {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
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
