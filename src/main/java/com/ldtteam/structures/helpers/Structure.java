package com.ldtteam.structures.helpers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

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
     * blueprint of the structure.
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
    protected final BlockPos.Mutable progressPos = new BlockPos.Mutable(-1, -1, -1);

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
            // Try the cache first
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
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(inputStream);
                this.blueprint = BlueprintUtil.readBlueprintFromNBT(CompoundNBT);
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
     * 
     * @param blueprint the blueprint to set.
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
     * Checks if the blueprint is null.
     *
     * @return true if the blueprint is null.
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
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        return getBlockInfo(pos).getState();
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
        return blueprint.getBlockInfoAsMap().get(pos);
    }

    /**
     * Getter of the EntityInfo at a certain position.
     *
     * @param pos the position.
     * @return the blockState.
     */
    @Nullable
    public CompoundNBT getTileEntityData(@NotNull final BlockPos pos)
    {
        return getBlockInfo(pos).getTileEntityData();
    }

    /**
     * Getter of the EntityInfo at the current position.
     *
     * @return the entity data.
     */
    @Nullable
    public CompoundNBT[] getEntityData()
    {
        return blueprint.getEntities();
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
     * 
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

    /**
     * Rotates the structure x times.
     *
     * @param rotation  the rotation.
     * @param world     world it's rotating it in.
     * @param rotatePos position to rotateWithMirror it around.
     * @param mirror    the mirror to rotate with.
     */
    public void rotate(final Rotation rotation, @NotNull final World world, @NotNull final BlockPos rotatePos, @NotNull final Mirror mirror)
    {
        this.offset = this.blueprint.rotateWithMirror(rotation, rotatePos, mirror, world);
    }

    /**
     * Increment progressPos.
     *
     * @return false if the all the block have been incremented through.
     */
    public boolean incrementBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(-1, 0, 0);
        }

        this.progressPos.setPos(this.progressPos.getX() + 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() >= this.blueprint.getSizeX())
        {
            this.progressPos.setPos(0, this.progressPos.getY(), this.progressPos.getZ() + 1);
            if (this.progressPos.getZ() >= this.blueprint.getSizeZ())
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() + 1, 0);
                if (this.progressPos.getY() >= this.blueprint.getSizeY())
                {
                    this.reset();
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Decrement progressPos.
     *
     * @return false if progressPos can't be decremented any more.
     */
    public boolean decrementBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(this.blueprint.getSizeX(), this.blueprint.getSizeY() - 1, this.blueprint.getSizeZ() - 1);
        }

        this.progressPos.setPos(this.progressPos.getX() - 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() <= -1)
        {
            this.progressPos.setPos(this.blueprint.getSizeX() - 1, this.progressPos.getY(), this.progressPos.getZ() - 1);
            if (this.progressPos.getZ() <= -1)
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() - 1, this.blueprint.getSizeZ() - 1);
                if (this.progressPos.getY() <= -1)
                {
                    this.reset();
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Find the next block that doesn't already exist in the world.
     *
     * @return true if a new block is found and false if there is no next block.
     */
    public boolean findNextBlock()
    {
        int count = 0;
        do
        {
            count++;
            if (!this.incrementBlock())
            {
                return false;
            }
        } while (StructurePlacementUtils.isStructureBlockEqualWorldBlock(world, getBlockPosition(), getBlockState(getLocalPosition())) &&
            count < Structurize.getConfig().getCommon().maxBlocksChecked.get());

        return true;
    }

    /**
     * Base position of the structure.
     *
     * @return BlockPos representing where the structure is.
     */
    public BlockPos getPosition()
    {
        if (this.position == null)
        {
            return new BlockPos(0, 0, 0);
        }
        return this.position;
    }

    /**
     * Calculate the item needed to place the current block in the structure.
     *
     * @return an item or null if not initialized.
     */
    @Nullable
    public Item getItem()
    {
        @Nullable
        final Block block = this.getBlock();
        @Nullable
        final BlockState blockState = this.getBlockstate();
        if (block == null || blockState == null || block == Blocks.AIR || blockState.getMaterial().isLiquid())
        {
            return null;
        }

        final ItemStack stack = BlockUtils.getItemStackFromBlockState(blockState);

        if (!ItemStackUtils.isEmpty(stack))
        {
            return stack.getItem();
        }

        return null;
    }

    /**
     * Calculate the current block in the structure.
     *
     * @return the current block or null if not initialized.
     */
    @Nullable
    public Block getBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }

        @Nullable
        final BlockState state = this.getBlockState(progressPos);
        if (state == null)
        {
            return null;
        }
        return state.getBlock();
    }

    /**
     * Calculate the current blockState in the structure.
     * 
     * @return the current blockState or null if not there.
     */
    @Nullable
    public BlockState getBlockstate()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.getBlockState(this.progressPos);
    }

    /**
     * Get the current blockinfo.
     * 
     * @return the current blockinfo or null if not there.
     */
    @Nullable
    public BlockInfo getBlockInfo()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.getBlockInfo(this.progressPos);
    }

    /**
     * Reset the progressPos.
     */
    public void reset()
    {
        BlockPosUtil.set(this.progressPos, NULL_POS);
    }

    /**
     * @return progressPos as an immutable.
     */
    @NotNull
    public BlockPos getLocalPosition()
    {
        return this.progressPos.toImmutable();
    }

    /**
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    public void setLocalPosition(@NotNull final BlockPos localPosition)
    {
        this.progressPos.setPos(localPosition.getX() % blueprint.getSizeX(), localPosition.getY() % blueprint.getSizeY(), localPosition.getZ() % blueprint.getSizeZ());
    }

    /**
     * @return World position.
     */
    public BlockPos getBlockPosition()
    {
        return this.progressPos.add(this.getOffsetPosition());
    }

    /**
     * @return Min world position for the structure.
     */
    public BlockPos getOffsetPosition()
    {
        return this.position.subtract(this.getOffset());
    }

    /**
     * Set the position, used when loading.
     *
     * @param position Where the structure is in the world.
     */
    public void setPosition(final BlockPos position)
    {
        this.position = position;
    }

    /**
     * Get the world instance we're placing in.
     * 
     * @return the world.
     */
    public World getWorld()
    {
        return world;
    }

    /**
     * Get the size and calculate it from a rotation.
     * 
     * @param rotation the rotation.
     * @param mirror   the mirror.
     * @return the rotated size.
     */
    public BlockPos getSize(final Rotation rotation, final Mirror mirror)
    {
        return Blueprint.transformedSize(new BlockPos(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ()), rotation);
    }
}
