package com.structurize.structures.blueprints.v1;

import java.util.List;

import com.structurize.coremod.blocks.interfaces.IAnchorBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
      IBlockState[] pallete,
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
        this.tileEntities = new NBTTagCompound[sizeX][sizeY][sizeZ];

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
        this.entities = new NBTTagCompound[sizeX][sizeY][sizeZ];
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
     * Rotate the structure depending on the direction it's facing.
     *
     * @param rotation  times to rotateWithMirror.
     * @param world     the world to rotateWithMirror it in.
     * @param pos the pos to rotateWithMirror it around.
     * @param mirror    the mirror.
     */
    public BlockPos rotateWithMirror(final Rotation rotation, final World world, final BlockPos pos, final Mirror mirror)
    {
        final BlockPos resultSize = transformedBlockPos(sizeX, sizeY, sizeZ, mirror, rotation);
        final short newSizeX = (short) resultSize.getX();
        final short newSizeY = (short) resultSize.getY();
        final short newSizeZ = (short) resultSize.getZ();

        final short[][][] newStructure = new short[newSizeX][newSizeY][newSizeZ];
        final NBTTagCompound[][][] newEntities = new NBTTagCompound[newSizeX][newSizeY][newSizeZ];
        final NBTTagCompound[][][] newTileEntities = new NBTTagCompound[newSizeX][newSizeY][newSizeZ];

        final IBlockState[] palette = new IBlockState[this.palette.length];
        for (int i = 0; i < palette.length; i++)
        {
            palette[i] = this.palette[i].withRotation(rotation).withMirror(mirror);
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
                    final IBlockState state = palette[value & 0xFFFF];
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
    private static BlockPos transformedBlockPos(final int xIn, final int y, final int zIn, final Mirror mirror, final Rotation rotation)
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
