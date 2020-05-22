package com.ldtteam.structurize.placement.structure;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Creative placement handler that doesn't require resources or tools.
 */
public class CreativeStructureHandler extends AbstractStructureHandler
{
    /**
     * If placement is supposed to be fancy or not.
     */
    public boolean fancyPlacement;

    /**
     * Creative constructor of structure handler.
     * @param world the world it gets.
     * @param pos the position the anchor of the structure got placed.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     * @param fancyPlacement if placement is fancy or complete.
     */
    public CreativeStructureHandler(final World world, final BlockPos pos, final String structureName, final PlacementSettings settings, final boolean fancyPlacement)
    {
        super(world, pos, structureName, settings);
        this.fancyPlacement = fancyPlacement;
    }

    /**
     * Creative constructor of structure handler.
     * @param world the world it gets.
     * @param pos the position the anchor of the structure got placed.
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     * @param fancyPlacement if placement is fancy or complete.

     */
    public CreativeStructureHandler(final World world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement)
    {
        super(world, pos, blueprint, settings);
        this.fancyPlacement = fancyPlacement;
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return null;
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> requiredItems)
    {
        // Do nothing. Override if needed.
    }

    @Override
    public int getMaxBlocksCheckedPerCall()
    {
        return Structurize.getConfig().getCommon().maxOperationsPerTick.get();
    }

    @Override
    public boolean isStackFree(@Nullable final ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean allowReplace()
    {
        return true;
    }

    @Override
    public ItemStack getHeldItem()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean replaceWithSolidBlock(final BlockState blockState)
    {
        return !blockState.getMaterial().isSolid();
    }

    @Override
    public boolean fancyPlacement()
    {
        return this.fancyPlacement;
    }

    @Override
    public boolean isCreative()
    {
        return true;
    }

    @Override
    public int getStepsPerCall()
    {
        return Structurize.getConfig().getCommon().maxOperationsPerTick.get();
    }

    @Override
    public boolean shouldBlocksBeConsideredEqual(final BlockState state, final BlockState state1)
    {
        return false;
    }

    @Override
    public boolean hasRequiredItems(@NotNull final List<ItemStack> requiredItems)
    {
        return true;
    }

    @Override
    public void prePlacementLogic(final BlockPos worldPos, final BlockState blockState)
    {
        // Do nothing
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(getWorld(), worldPos);
    }
}
