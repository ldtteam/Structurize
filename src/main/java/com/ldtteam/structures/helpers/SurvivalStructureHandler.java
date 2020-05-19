package com.ldtteam.structures.helpers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * A handler for structures. To place a structure a handler is required.
 */
public class SurvivalStructureHandler extends AbstractStructureHandler
{
    private final IItemHandler inv;

    /**
     * Creative constructor of structure handler.
     * @param world the world it gets.
     * @param pos the position the anchor of the structure got placed.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     */
    public SurvivalStructureHandler(final World world, final BlockPos pos, final String structureName, final PlacementSettings settings, final IItemHandler handler)
    {
        super(world, pos, structureName, settings);
        this.inv = handler;
    }

    /**
     * Creative constructor of structure handler.
     * @param world the world it gets.
     * @param pos the position the anchor of the structure got placed.
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     */
    public SurvivalStructureHandler(final World world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final IItemHandler handler)
    {
        super(world, pos, blueprint, settings);
        this.inv = handler;
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return this.inv;
    }

    @Override
    public void triggerSuccess(final BlockPos pos)
    {

    }

    @Override
    public boolean isCreative()
    {
        return false;
    }

    @Override
    public int getStepsPerCall()
    {
        return 1;
    }

    @Override
    public int getMaxBlocksCheckedPerCall()
    {
        return 1;
    }

    @Override
    public boolean isStackFree(@Nullable final ItemStack stack)
    {
        return false;
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
        return true;
    }
}
