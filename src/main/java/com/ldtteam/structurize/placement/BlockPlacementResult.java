package com.ldtteam.structurize.placement;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Possible placement result.
 */
public class BlockPlacementResult
{
    /**
     * Required items for placement.
     */
    private final List<ItemStack> requiredItems = new ArrayList<>();

    /**
     * World pos of placement
     */
    private final BlockPos worldPos;

    /**
     * The result of the placement.
     */
    private final Result result;

    /**
     * Create a placement result object.
     * @param worldPos the pos in the world.
     * @param result the result.
     */
    public BlockPlacementResult(final BlockPos worldPos, final Result result)
    {
        this.worldPos = worldPos;
        this.result = result;
    }

    /**
     * Create a placement result object.
     * @param worldPos the pos in the world.
     * @param result the result.
     * @param requiredItems the missing items.
     */
    public BlockPlacementResult(final BlockPos worldPos, final Result result, final List<ItemStack> requiredItems)
    {
        this.worldPos = worldPos;
        this.result = result;
        this.requiredItems.addAll(requiredItems);
    }

    /**
     * Get the list of required items.
     * @return a copy of the list.
     */
    public List<ItemStack> getRequiredItems()
    {
        return new ArrayList<>(this.requiredItems);
    }

    /**
     * Get the world position.
     * @return the world blockpos.
     */
    public BlockPos getWorldPos()
    {
        return worldPos;
    }

    /**
     * Get the placement result type.
     * @return the result type.
     */
    public Result getResult()
    {
        return result;
    }

    /**
     * Possible results for placement.
     */
    public enum Result
    {
        SUCCESS,
        MISSING_ITEMS,
        BREAK_BLOCK,
        FINISHED,
        FAIL,
        LIMIT_REACHED
    }
}
