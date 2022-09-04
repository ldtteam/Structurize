package com.ldtteam.structurize.placement.structure;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Future;

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
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     * @param fancyPlacement if placement is fancy or complete.
     */
    public CreativeStructureHandler(final Level world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement)
    {
        super(world, pos, blueprint, settings);
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
    public CreativeStructureHandler(final Level world, final BlockPos pos, final Future<Blueprint> blueprint, final PlacementSettings settings, final boolean fancyPlacement)
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
    public void triggerEntitySuccess(final BlockPos pos, final List<ItemStack> requiredRes, final boolean placement)
    {
        // Do nothing. Override if needed.
    }

    @Override
    public int getMaxBlocksCheckedPerCall()
    {
        return Structurize.getConfig().getServer().maxOperationsPerTick.get();
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
        return Structurize.getConfig().getServer().maxOperationsPerTick.get();
    }

    @Override
    public boolean shouldBlocksBeConsideredEqual(final BlockState state, final BlockState state1)
    {
        return false;
    }

    @Override
    public boolean hasRequiredItems(final List<ItemStack> requiredItems)
    {
        return true;
    }

    @Override
    public void prePlacementLogic(final BlockPos worldPos, final BlockState blockState, final List<ItemStack> requiredItems)
    {
        // Do nothing
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(getWorld(), worldPos, null);
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos, final BlockState virtualBlockAbove)
    {
        return BlockUtils.getSubstitutionBlockAtWorld(getWorld(), worldPos, virtualBlockAbove);
    }
}
