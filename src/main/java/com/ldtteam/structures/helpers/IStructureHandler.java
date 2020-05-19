package com.ldtteam.structures.helpers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.ldtteam.structurize.util.StructureUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

//todo add to minecolonies ores to solidSubStitutionPredicateList
//todo Minecolonies implement this with the AI.
// return (block1 == Blocks.GRASS_BLOCK && block2 == Blocks.DIRT) || block2 == Blocks.GRASS_BLOCK && block1 == Blocks.DIRT;

/**
 * A handler for structures. To place a structure a handler is required.
 */
public interface IStructureHandler
{
    /**
     * Load the blueprint from the file name.
     *
     * @param structureName name of the structure (at stored location).
     */
    default void loadBlueprint(final String structureName)
    {
        String correctStructureName = structureName;
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
                setMd5(StructureUtils.calculateMD5(StructureLoadingUtils.getStream(correctStructureName)));
                final CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(inputStream);
                setBlueprint(BlueprintUtil.readBlueprintFromNBT(CompoundNBT));
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
     * Set the blueprint.
     * @param blueprint the blueprint to set.
     */
    void setBlueprint(Blueprint blueprint);

    /**
     * Set the md5 value.
     * @param calculatedMD5 the calculated md5.
     */
    void setMd5(String calculatedMD5);

    /**
     * Get the md5 value.
     * @return the md5.
     */
    String getMd5();

    /**
     * Compare the md5 from the structure with an other md5 hash.
     *
     * @param otherMD5 to compare with
     * @return whether the otherMD5 match, return false if md5 is null
     */
    default boolean isCorrectMD5(final String otherMD5)
    {
        Log.getLogger().info("isCorrectMD5: md5:" + this.getMd5() + " other:" + otherMD5);
        if (this.getMd5() == null || otherMD5 == null)
        {
            return false;
        }
        return this.getMd5().compareTo(otherMD5) == 0;
    }

    /**
     * Get the bluerint from the handler.
     * @return the blueprint
     */
    Blueprint getBluePrint();

    /**
     * Get the world from the handler.
     * @return the world.
     */
    World getWorld();

    /**
     * Get the world position this is placed at.
     * @return the position.
     */
    BlockPos getWorldPos();

    /**
     * Getter for the placement settings.
     * @return the settings object.
     */
    PlacementSettings getSettings();

    /**
     * Get the inventory of the handler.
     * @return the IItemhandler (may be null!).
     */
    @Nullable
    IItemHandler getInventory();

    /**
     * Trigger success AFTER placement of block.
     * @param pos the pos it was placed at.
     */
    void triggerSuccess(final BlockPos pos);

    /**
     * If creative placement (Free placement without inventory).
     * @return true if so.
     */
    boolean isCreative();

    /**
     * Check if the handler has a valid blueprint.
     * @return true if so.
     */
    boolean hasBluePrint();

    /**
     * How many steps are executed on per call.
     * @return the number of steps.
     */
    int getStepsPerCall();

    /**
     * How many blocks are checked max per call.
     * @return the max number.
     */
    int getMaxBlocksCheckedPerCall();

    /**
     * Check if the stack is free for this handler.
     * @param stack the stack to check.
     * @return true if so.
     */
    boolean isStackFree(@Nullable final ItemStack stack);

    /**
     * If the handler allows to replace or requires to mine the block first.
     * @return true if so.
     */
    boolean allowReplace();

    /**
     * The item being held by the handler
     * @return the Item (could be empty).
     */
    ItemStack getHeldItem();

    /**
     * Check if this block is considered solid for the solid placerholder blocks.
     * @param blockState the blockState to check.
     * @return true if it is solid.
     */
    boolean replaceWithSolidBlock(BlockState blockState);

    /**
     * If this is supposed to be fancy placement (player facing) or builder facing (complete).
     * @return true if fancy placement.
     */
    boolean fancyPlacement();
}
