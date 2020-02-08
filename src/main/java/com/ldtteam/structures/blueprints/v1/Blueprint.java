package com.ldtteam.structures.blueprints.v1;

import com.ldtteam.structures.client.BlueprintBlockInfoTransformHandler;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;
import com.ldtteam.structurize.util.BlockInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.Constants.NINETY_DEGREES;

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
    private List<BlockState> palette;

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
    private CompoundNBT[][][] tileEntities;

    /**
     * The entities.
     */
    private CompoundNBT[] entities = new CompoundNBT[0];

    /**
     * Various caches for storing block data in prepared structures
     */
    private List<BlockInfo> cacheBlockInfo = null;
    private Map<BlockPos, BlockInfo> cacheBlockInfoMap = null;

    /**
     * Cache for storing rotate/mirror anchor
     */
    private BlockPos cachePrimaryOffset = null;

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
        List<BlockState> pallete,
        short[][][] structure,
        CompoundNBT[] tileEntities,
        List<String> requiredMods)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.palleteSize = palleteSize;
        this.palette = pallete;
        this.structure = structure;
        this.tileEntities = new CompoundNBT[sizeY][sizeZ][sizeX];

        for (final CompoundNBT te : tileEntities)
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
     * @param sizeX the x size.
     * @param sizeY the y size.
     * @param sizeZ the z size.
     */
    public Blueprint(short sizeX, short sizeY, short sizeZ)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.structure = new short[sizeY][sizeZ][sizeX];
        this.tileEntities = new CompoundNBT[sizeY][sizeZ][sizeX];

        this.requiredMods = new ArrayList<>();
        this.palette = new ArrayList<>();
        this.palette.add(0, ModBlocks.blockSubstitution.getDefaultState());
    }

    /**
     * @return the Size of the Structure on the X-Axis (without rotation and/or
     *         mirroring)
     */
    public short getSizeX()
    {
        return this.sizeX;
    }

    /**
     * @return the Size of the Structure on the Y-Axis (without rotation and/or
     *         mirroring)
     */
    public short getSizeY()
    {
        return this.sizeY;
    }

    /**
     * @return the Size of the Structure on the Z-Axis (without rotation and/or
     *         mirroring)
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
    public BlockState[] getPalette()
    {
        return this.palette.toArray(new BlockState[0]);
    }

    /**
     * Add a blockstate to the structure.
     *
     * @param pos   the position to add it to.
     * @param state the state to add.
     */
    public void addBlockState(final BlockPos pos, final BlockState state)
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
        cacheReset();
    }

    /**
     * @return the structure (without rotation and/or mirroring)
     *         The Coordinate order is: y, z, x
     */
    public short[][][] getStructure()
    {
        return this.structure;
    }

    /**
     * @return an array of serialized TileEntities (posX, posY and posZ tags have
     *         been localized to coordinates within the structure)
     */
    public CompoundNBT[][][] getTileEntities()
    {
        return this.tileEntities;
    }

    /**
     * @return an array of serialized TileEntities (the Pos tag has
     *         been localized to coordinates within the structure)
     */
    public CompoundNBT[] getEntities()
    {
        return this.entities;
    }

    /**
     * @param entities an array of serialized TileEntities (the Pos tag need to
     *                 be localized to coordinates within the structure)
     */
    public void setEntities(CompoundNBT[] entities)
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
     *
     * @param name the name to set.
     * @return this object.
     */
    public Blueprint setName(final String name)
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
     * 
     * @param architects an array of architects.
     * @return this blueprint.
     */
    public Blueprint setArchitects(final String[] architects)
    {
        this.architects = architects;
        return this;
    }

    /**
     * @return An Array of all missing mods that are required to generate this
     *         structure (only works if structure was loaded from file)
     */
    public String[] getMissingMods()
    {
        return this.missingMods;
    }

    /**
     * Sets the missing mods
     * 
     * @param missingMods the missing mods list.
     * @return this object.
     */
    public Blueprint setMissingMods(final String... missingMods)
    {
        this.missingMods = missingMods;
        return this;
    }

    /**
     * Get a list of all entities in the blueprint as a list.
     *
     * @return the list of CompoundNBTs.
     */
    public final List<CompoundNBT> getEntitiesAsList()
    {
        return Arrays.stream(entities).collect(Collectors.toList());
    }

    /**
     * Get a list of all blockInfo objects in the blueprint.
     *
     * @return a list of all blockinfo (position, blockState, tileEntityData).
     */
    public final List<BlockInfo> getBlockInfoAsList()
    {
        if (cacheBlockInfo == null)
        {
            buildBlockInfoCaches();
        }
        return cacheBlockInfo;
    }

    /**
     * Get a map of all blockpos->blockInfo objects in the blueprint.
     *
     * @return a map of all blockpos->blockInfo (position, blockState, tileEntityData).
     */
    public final Map<BlockPos, BlockInfo> getBlockInfoAsMap()
    {
        if (cacheBlockInfoMap == null)
        {
            buildBlockInfoCaches();
        }
        return cacheBlockInfoMap;
    }

    private final void buildBlockInfoCaches()
    {
        cacheBlockInfo = new ArrayList<>(getVolume());
        cacheBlockInfoMap = new HashMap<>(getVolume());
        for (short x = 0; x < this.sizeX; x++)
        {
            for (short y = 0; y < this.sizeY; y++)
            {
                for (short z = 0; z < this.sizeZ; z++)
                {
                    final BlockPos tempPos = new BlockPos(x, y, z);
                    final BlockInfo blockInfo = new BlockInfo(tempPos, palette.get(structure[y][z][x] & 0xFFFF), tileEntities[y][z][x]);
                    cacheBlockInfo.add(blockInfo);
                    cacheBlockInfoMap.put(tempPos, blockInfo);
                }
            }
        }
    }

    public final BlockPos getPrimaryBlockOffset()
    {
        if (cachePrimaryOffset == null)
        {
            cachePrimaryOffset = findPrimaryBlockOffset();
        }
        return cachePrimaryOffset;
    }

    private final BlockPos findPrimaryBlockOffset()
    {
        final List<BlockInfo> list =
            getBlockInfoAsList().stream().filter(blockInfo -> blockInfo.getState().getBlock() instanceof IAnchorBlock).collect(Collectors.toList());

        if (list.size() != 1)
        {
            return new BlockPos(sizeX / 2, 0, sizeZ / 2);
        }
        return BlueprintBlockInfoTransformHandler.getInstance().Transform(list.get(0)).getPos();
    }

    private final void cacheReset()
    {
        cacheBlockInfo = null;
        cachePrimaryOffset = null;
        cacheBlockInfoMap = null;
    }

    /**
     * Rotate the structure depending on the direction it's facing.
     *
     * @param rotation times to rotateWithMirror.
     * @param pos      the pos to rotateWithMirror it around.
     * @param mirror   the mirror.
     * @param world    the world.
     * @return the new offset.
     */
    public BlockPos rotateWithMirror(final Rotation rotation, final BlockPos pos, final Mirror mirror, final World world)
    {
        final BlockPos resultSize = transformedSize(new BlockPos(sizeX, sizeY, sizeZ), rotation);
        final short newSizeX = (short) resultSize.getX();
        final short newSizeY = (short) resultSize.getY();
        final short newSizeZ = (short) resultSize.getZ();

        final short[][][] newStructure = new short[newSizeY][newSizeZ][newSizeX];
        final CompoundNBT[] newEntities = new CompoundNBT[entities.length];
        final CompoundNBT[][][] newTileEntities = new CompoundNBT[newSizeY][newSizeZ][newSizeX];

        final List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < this.palette.size(); i++)
        {
            palette.add(i, this.palette.get(i).mirror(mirror).rotate(rotation));
        }

        final BlockPos extremes = transformedBlockPos(sizeX, sizeY, sizeZ, mirror, rotation);
        int minX = extremes.getX() < 0 ? -extremes.getX() - 1 : 0;
        int minY = extremes.getY() < 0 ? -extremes.getY() - 1 : 0;
        int minZ = extremes.getZ() < 0 ? -extremes.getZ() - 1 : 0;

        this.palette = palette;

        boolean foundAnchor = false;
        BlockPos offset = pos;
        boolean multipleAnchors = false;

        for (short x = 0; x < this.sizeX; x++)
        {
            for (short y = 0; y < this.sizeY; y++)
            {
                for (short z = 0; z < this.sizeZ; z++)
                {
                    final BlockPos tempPos = transformedBlockPos(x, y, z, mirror, rotation).add(minX, minY, minZ);
                    final short value = structure[y][z][x];
                    final BlockState state = palette.get(value & 0xFFFF);
                    if (state.getBlock() == Blocks.STRUCTURE_VOID)
                    {
                        continue;
                    }
                    if (state.getBlock() instanceof IAnchorBlock)
                    {
                        offset = tempPos;
                        if (foundAnchor)
                        {
                            multipleAnchors = true;
                        }
                        foundAnchor = true;
                    }
                    newStructure[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = value;

                    final CompoundNBT compound = tileEntities[y][z][x];
                    if (compound != null)
                    {
                        compound.putInt("x", tempPos.getX());
                        compound.putInt("y", tempPos.getY());
                        compound.putInt("z", tempPos.getZ());
                    }
                    newTileEntities[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = compound;
                }
            }
        }

        for (int i = 0; i < entities.length; i++)
        {
            final CompoundNBT entitiesCompound = entities[i];
            if (entitiesCompound != null)
            {
                newEntities[i] = transformEntityInfoWithSettings(entitiesCompound, world, new BlockPos(minX, minY, minZ), rotation, mirror);
            }
        }

        BlockPos temp;
        if (rotation.equals(Rotation.CLOCKWISE_90) || rotation.equals(Rotation.COUNTERCLOCKWISE_90) || mirror.equals(Mirror.FRONT_BACK))
        {
            if (minX == minZ)
            {
                temp = new BlockPos(resultSize.getX(), resultSize.getY(), minZ > 0 ? -resultSize.getZ() : resultSize.getZ());
            }
            else
            {
                temp = new BlockPos(minX > 0 ? -resultSize.getX() : resultSize.getX(), resultSize.getY(), minZ > 0 ? -resultSize.getZ() : resultSize.getZ());
            }

            Rotation theRotation = rotation;
            if (rotation == Rotation.CLOCKWISE_90)
            {
                theRotation = Rotation.COUNTERCLOCKWISE_90;
            }
            else if (rotation == Rotation.COUNTERCLOCKWISE_90)
            {
                theRotation = Rotation.CLOCKWISE_90;
            }

            temp = temp.rotate(theRotation);
        }
        else
        {
            temp = resultSize;
        }

        if (!foundAnchor || multipleAnchors)
        {
            BlockPos tempSize = new BlockPos(temp.getX(), 0, temp.getZ());
            if (rotation == Rotation.CLOCKWISE_90)
            {
                tempSize = new BlockPos(-temp.getZ(), 0, temp.getX());
            }
            if (rotation == Rotation.CLOCKWISE_180)
            {
                tempSize = new BlockPos(-temp.getX(), 0, -temp.getZ());
            }
            if (rotation == Rotation.COUNTERCLOCKWISE_90)
            {
                tempSize = new BlockPos(temp.getZ(), 0, -temp.getX());
            }

            offset = new BlockPos(tempSize.getX() / 2, 0, tempSize.getZ() / 2).add(minX, minY, minZ);
        }

        sizeX = newSizeX;
        sizeY = newSizeY;
        sizeZ = newSizeZ;

        this.structure = newStructure;
        this.entities = newEntities;
        this.tileEntities = newTileEntities;

        cacheReset();
        return offset;
    }

    /**
     * Calculate the transformed size from a blockpos.
     *
     * @param pos      the pos to transform
     * @param rotation the rotation to apply.
     * @return the resulting size.
     */
    public static BlockPos transformedSize(final BlockPos pos, final Rotation rotation)
    {
        switch (rotation)
        {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new BlockPos(pos.getZ(), pos.getY(), pos.getX());
            default:
                return pos;
        }
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
                return flag ? new BlockPos(x, y, z) : new BlockPos(xIn, y, zIn);
        }
    }

    /**
     * Transform an entity and rotate it.
     *
     * @param entityInfo the entity nbt.
     * @param world      the world.
     * @param pos        the position.
     * @param rotation   the wanted rotation.
     * @param mirror     the mirror.
     * @return the updated nbt.
     */
    private CompoundNBT transformEntityInfoWithSettings(
        final CompoundNBT entityInfo,
        final World world,
        final BlockPos pos,
        final Rotation rotation,
        final Mirror mirror)
    {
        final Optional<EntityType<?>> type = EntityType.readEntityType(entityInfo);
        if (type.isPresent())
        {
            final Entity finalEntity = type.get().create(world);
            if (finalEntity != null)
            {
                finalEntity.deserializeNBT(entityInfo);

                final Vec3d entityVec = Blueprint.transformedVec3d(rotation, mirror, finalEntity.getPositionVector()).add(new Vec3d(pos));
                finalEntity.prevRotationYaw = (float) (finalEntity.getMirroredYaw(mirror) - NINETY_DEGREES);
                final double rotationYaw = finalEntity.getMirroredYaw(mirror) + ((double) finalEntity.getMirroredYaw(mirror) - (double) finalEntity.getRotatedYaw(rotation));

                if (finalEntity instanceof HangingEntity)
                {
                    final BlockPos currentPos = ((HangingEntity) finalEntity).getHangingPosition();
                    final BlockPos entityPos = Blueprint.transformedBlockPos(currentPos.getX(), currentPos.getY(), currentPos.getZ(), mirror, rotation).add(pos);

                    finalEntity.setPosition(entityVec.x, entityVec.y, entityVec.z);
                    finalEntity.setPosition(entityPos.getX(), entityPos.getY(), entityPos.getZ());
                }
                else
                {
                    finalEntity.setLocationAndAngles(entityVec.x, entityVec.y, entityVec.z, (float) rotationYaw, finalEntity.rotationPitch);
                }

                return finalEntity.serializeNBT();
            }
        }
        return null;
    }

    /**
     * Transform a Vec3d with rotation and mirror.
     *
     * @param rotation the rotation.
     * @param mirror   the mirror.
     * @param vec      the vec to transform.
     * @return the result.
     */
    private static Vec3d transformedVec3d(final Rotation rotation, final Mirror mirror, final Vec3d vec)
    {
        double xCoord = vec.x;
        double zCoord = vec.z;
        boolean flag = true;

        switch (mirror)
        {
            case LEFT_RIGHT:
                zCoord = 1.0D - zCoord;
                break;
            case FRONT_BACK:
                xCoord = 1.0D - xCoord;
                break;
            default:
                flag = false;
        }

        switch (rotation)
        {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(zCoord, vec.y, 1.0D - xCoord);
            case CLOCKWISE_90:
                return new Vec3d(1.0D - zCoord, vec.y, xCoord);
            case CLOCKWISE_180:
                return new Vec3d(1.0D - xCoord, vec.y, 1.0D - zCoord);
            default:
                return flag ? new Vec3d(xCoord, vec.y, zCoord) : vec;
        }
    }

    private int getVolume()
    {
        return (int) sizeX * sizeY * sizeZ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + palleteSize;
        result = prime * result + getVolume();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || !(obj instanceof Blueprint))
        {
            return false;
        }
        final Blueprint other = (Blueprint) obj;
        if (!name.equals(other.name) || palleteSize != other.palleteSize || getVolume() != other.getVolume())
        {
            return false;
        }
        return true;
    }
}
