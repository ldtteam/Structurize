package com.ldtteam.structurize.placement.structure;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import com.ldtteam.structurize.util.StructureUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A handler for structures.
 * Handlers hold necessary and specific information about the entity/block/etc that is executing the placement.
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
                final byte[] data = StructureLoadingUtils.getStreamAsByteArray(inputStream);
                inputStream.close();
                setMd5(StructureUtils.calculateMD5(data));
                final CompoundTag CompoundNBT = NbtIo.readCompressed(new ByteArrayInputStream(data));
                final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(CompoundNBT);
                blueprint.setFileName(structureName);
                setBlueprint(blueprint);
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
    Level getWorld();

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
     * @param requiredRes the list of required res.
     * @param placement if through placement (true) or through equality (false).
     */
    void triggerSuccess(final BlockPos pos, final List<ItemStack> requiredRes, final boolean placement);

    /**
     * Trigger entity success.
     * @param pos the position the entity was placed at.
     * @param requiredRes the list of required res.
     * @param placement if through placement (true) or through equality (false).
     */
    void triggerEntitySuccess(final BlockPos pos, final List<ItemStack> requiredRes, final boolean placement);

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
     * @return true if it should be replaced.
     */
    boolean replaceWithSolidBlock(BlockState blockState);

    /**
     * If this is supposed to be fancy placement (player facing) or builder facing (complete).
     * @return true if fancy placement.
     */
    boolean fancyPlacement();

    /**
     * Special equal condition check.
     * @param state the first state.
     * @param state1 the second state.
     * @return true if considered equal and block should be skipped.
     */
    boolean shouldBlocksBeConsideredEqual(BlockState state, BlockState state1);

    /**
     * Check if the handler has the required items for an action.
     * @param requiredItems the list of items.
     * @return true if so.
     */
    boolean hasRequiredItems(final List<ItemStack> requiredItems);

    /**
     * Get the position in the world translated from a local pos in the structure.
     * @param localPos the local structure pos.
     * @return the world pos.
     */
    default BlockPos getProgressPosInWorld(final BlockPos localPos)
    {
        return getWorldPos().subtract(getBluePrint().getPrimaryBlockOffset()).offset(localPos);
    }

    /**
     * Execute pre placement logic if necessary.
     * @param worldPos the position the block si going to be placed.
     * @param blockState the blockstate to be placed.
     * @param requiredItems the list of required items.
     */
    void prePlacementLogic(final BlockPos worldPos, final BlockState blockState, final List<ItemStack> requiredItems);

    /**
     * Get the right solid block for the substitution block.
     * @param worldPos the world pos.
     * @return the right block (classically biome dependent).
     */
    @Deprecated(forRemoval = true, since = "1.18.2")
    BlockState getSolidBlockForPos(BlockPos worldPos);

    /**
     * Get the solid worldgen block for given pos while using data from handler.
     * @param worldPos          the world pos.
     * @param virtualBlockAbove block that is gonna be place above given worldPos, null if unknown
     * @return the solid worldgen block (classically biome dependent).
     */
    BlockState getSolidBlockForPos(BlockPos worldPos, @Nullable BlockState virtualBlockAbove);
}
