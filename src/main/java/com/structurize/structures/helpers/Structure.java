package com.structurize.structures.helpers;

import com.structurize.api.util.Log;
import com.structurize.coremod.management.Structures;
import com.structurize.coremod.util.BlockInfo;
import com.structurize.coremod.util.PlacementSettings;
import com.structurize.coremod.util.StructureLoadingUtils;
import com.structurize.coremod.util.StructureUtils;
import com.structurize.structures.blueprints.v1.Blueprint;
import com.structurize.structures.blueprints.v1.BlueprintUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

import static com.structurize.coremod.management.Structures.SCHEMATIC_EXTENSION_NEW;

/**
 * Structure class, used to store, create, get structures.
 */
public class Structure
{
    /**
     * The position we use as our uninitialized value.
     */
    protected static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);

    /**
     * Template of the structure.
     */
    private Blueprint blueprint;

    /**
     * The MD5 value of the blueprint.
     */
    private String md5;

    /**
     * The used settings for the placement.
     */
    private PlacementSettings settings;

    /**
     * The current offset.
     */
    private BlockPos offset;

    /**
     * The minecraft world this struture is displayed in.
     */
    @NotNull
    protected final World world;

    /**
     * The anchor position this structure will be
     * placed on in the minecraft world.
     */
    protected BlockPos position;

    /**
     * The Structure position we are at. Defaulted to NULL_POS.
     */
    protected final BlockPos.MutableBlockPos progressPos = new BlockPos.MutableBlockPos(-1, -1, -1);

    /**
     * Constuctor of Structure, tries to create a new structure.
     * creates a plain Structure to append rendering later.
     *
     * @param world with world.
     */
    public Structure(@NotNull final World world)
    {
        this.world = world;
    }

    /**
     * Constuctor of Structure, tries to create a new structure.
     *
     * @param world         with world.
     * @param structureName name of the structure (at stored location).
     * @param settings      it's settings.
     */
    public Structure(@NotNull final World world, final String structureName, final PlacementSettings settings)
    {
        this(world);
        String correctStructureName = structureName;
        this.settings = settings;

        InputStream inputStream = null;
        try
        {
            //Try the cache first
            if (Structures.hasMD5(correctStructureName))
            {
                inputStream = StructureLoadingUtils.getStream(Structures.SCHEMATICS_CACHE + '/' + Structures.getMD5(correctStructureName));
                if (inputStream != null)
                {
                    correctStructureName = Structures.SCHEMATICS_CACHE + '/' + Structures.getMD5(correctStructureName);
                }
            }

            if (inputStream == null)
            {
                inputStream = StructureLoadingUtils.getStream(correctStructureName);
            }

            if (inputStream == null)
            {
                return;
            }

            try
            {
                this.md5 = StructureUtils.calculateMD5(StructureLoadingUtils.getStream(correctStructureName));
                final String ending = Structures.getFileExtension(correctStructureName);
                if (ending != null)
                {
                    if (ending.endsWith(SCHEMATIC_EXTENSION_NEW))
                    {
                        final NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(inputStream);
                        this.blueprint = BlueprintUtil.readBlueprintFromNBT(nbttagcompound, StructureUtils.getFixer());
                    }
                }
            }
            catch (final IOException e)
            {
                Log.getLogger().warn(String.format("Failed to load blueprint %s", correctStructureName), e);
            }
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * get the blueprint from the structure.
     *
     * @return The blueprint for the structure
     */
    public Blueprint getBluePrint()
    {
        return this.blueprint;
    }

    /**
     * Set the blueprint externally.
     * @param blueprint the template to set.
     */
    public void setBluePrint(final Blueprint blueprint)
    {
        this.blueprint = blueprint;
    }

    /**
     * Compare the md5 from the structure with an other md5 hash.
     *
     * @param otherMD5 to compare with
     * @return whether the otherMD5 match, return false if md5 is null
     */
    public boolean isCorrectMD5(final String otherMD5)
    {
        Log.getLogger().info("isCorrectMD5: md5:" + this.md5 + " other:" + otherMD5);
        if (this.md5 == null || otherMD5 == null)
        {
            return false;
        }
        return this.md5.compareTo(otherMD5) == 0;
    }

    /**
     * Checks if the template is null.
     *
     * @return true if the template is null.
     */
    public boolean isBluePrintMissing()
    {
        return this.blueprint == null;
    }

    /**
     * Getter of the offset.
     *
     * @return the blockPos of the offset.
     */
    public BlockPos getOffset()
    {
        return this.offset;
    }

    /**
     * Setter of the offset.
     *
     * @param pos the new offset.
     */
    public void setOffset(final BlockPos pos)
    {
        this.offset = pos;
    }

    /**
     * Getter of the IBlockState at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    @Nullable
    public IBlockState getBlockState(@NotNull final BlockPos pos)
    {
        if (this.blueprint.getStructure().length <= pos.getX() || this.blueprint.getStructure()[pos.getX()].length <= pos.getY() || this.blueprint.getStructure()[pos.getX()][pos.getY()].length <= pos.getZ())
        {
            return null;
        }
        return this.blueprint.getPalette()[this.blueprint.getStructure()[pos.getX()][pos.getY()][pos.getZ()] & 0xFFFF];
    }

    /**
     * Getter of the BlockInfo at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    @NotNull
    public BlockInfo getBlockInfo(@NotNull final BlockPos pos)
    {
        final IBlockState state = getBlockState(pos);
        final NBTTagCompound compound = this.getTileEntityData(pos);
        return new BlockInfo(pos, state, compound);
    }

    /**
     * Getter of the EntityInfo at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    @Nullable
    public NBTTagCompound getTileEntityData(@NotNull final BlockPos pos)
    {
        if (this.blueprint.getTileEntities().length <= pos.getX() || this.blueprint.getTileEntities()[pos.getX()].length <= pos.getY() || this.blueprint.getTileEntities()[pos.getX()][pos.getY()].length <= pos.getZ())
        {
            return null;
        }
        return this.blueprint.getTileEntities()[pos.getX()][pos.getY()][pos.getZ()];
    }

    /**
     * Getter of the EntityInfo at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    @Nullable
    public NBTTagCompound getEntityData(@NotNull final BlockPos pos)
    {
        if (this.blueprint.getEntities().length <= pos.getX() || this.blueprint.getEntities()[pos.getX()].length <= pos.getY() || this.blueprint.getEntities()[pos.getX()][pos.getY()].length <= pos.getZ())
        {
            return null;
        }
        return this.blueprint.getEntities()[pos.getX()][pos.getY()][pos.getZ()];
    }

    /**
     * Getter of the width.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return this.blueprint.getSizeX();
    }

    /**
     * Getter of the length.
     *
     * @return the length
     */
    public int getLength()
    {
        return this.blueprint.getSizeZ();
    }

    /**
     * Getter of the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return this.blueprint.getSizeY();
    }

    /**
     * Set the placement settings of the structure.
     * @param settings the settings to set.
     */
    public void setPlacementSettings(final PlacementSettings settings)
    {
        this.settings = settings;
    }

    /**
     * Get the Placement settings of the structure.
     *
     * @return the settings.
     */
    public PlacementSettings getSettings()
    {
        return this.settings;
    }
}
