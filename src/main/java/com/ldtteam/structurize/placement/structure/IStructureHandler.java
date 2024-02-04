package com.ldtteam.structurize.placement.structure;

import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.util.InventoryUtils;
import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * A handler for structures.
 * Handlers hold necessary and specific information about the entity/block/etc that is executing the placement.
 */
public interface IStructureHandler
{
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
    RotationMirror getRotationMirror();

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
     * Consume the items from the handler inventory if existent.
     * @param requiredItems the items to consume.
     */
    default void consume(final List<ItemStack> requiredItems)
    {
        if (this.getInventory() != null)
        {
            for (final ItemStack tempStack : requiredItems)
            {
                if (!ItemStackUtils.isEmpty(tempStack))
                {
                    InventoryUtils.consumeStack(tempStack, this.getInventory());
                }
            }
        }
    }

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
     * Get the position in the blueprint translated from world pos.
     * @param worldPos the world pos.
     * @return the structure pos.
     */
    default BlockPos getStructurePosFromWorld(final BlockPos worldPos)
    {
        return getBluePrint().getPrimaryBlockOffset().offset(worldPos.subtract(getWorldPos()));
    }

    /**
     * Execute pre placement logic if necessary.
     * @param worldPos the position the block si going to be placed.
     * @param blockState the blockstate to be placed.
     * @param requiredItems the list of required items.
     */
    void prePlacementLogic(final BlockPos worldPos, final BlockState blockState, final List<ItemStack> requiredItems);

    /**
     * Get the solid worldgen block for given pos while using data from handler.
     * 
     * @param  worldPos      the world pos.
     * @param  virtualBlocks if null use level instead for getting surrounding block states, fnc may should return null if virtual
     *                       block is not available
     * @return               the solid worldgen block (classically biome dependent).
     */
    BlockState getSolidBlockForPos(BlockPos worldPos, @Nullable Function<BlockPos, BlockState> virtualBlocks);

    /**
     * Check if the handler is ready.
     * @return true if so.
     */
    boolean isReady();
}
