package com.ldtteam.structures.helpers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * A handler for structures. To place a structure a handler is required.
 */
public class CreativeStructureHandler extends AbstractStructureHandler
{
    /**
     * Creative constructor of structure handler.
     * @param world the world it gets.
     * @param pos the position the anchor of the structure got placed.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     */
    public CreativeStructureHandler(final World world, final BlockPos pos, final String structureName, final PlacementSettings settings)
    {
        super(world, pos, structureName, settings);
    }

    /**
     * Creative constructor of structure handler.
     * @param world the world it gets.
     * @param pos the position the anchor of the structure got placed.
     * @param blueprint the blueprint.
     * @param settings the placement settings.
     */
    public CreativeStructureHandler(final World world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings)
    {
        super(world, pos, blueprint, settings);
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return null;
    }

    @Override
    public boolean breakBlock(final BlockPos pos)
    {
        return true;
    }

    @Override
    public void triggerSuccess(final BlockPos pos)
    {

    }

    @Override
    public boolean isCreative()
    {
        return true;
    }
}
