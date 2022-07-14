package com.ldtteam.structurize.blockentities.interfaces;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.AbstractStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Special creative placement handler anchors.
 */
public interface ISpecialCreativeHandler
{
    /**
     * Get the special structure handler.
     */
    AbstractStructureHandler getStructureHandler(final Level world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement);

    /**
     * Pre-setup before running the placement.
     *
     * @param player the player attempting the placement.
     * @param world the world to run it for.
     * @param pos the pos to run it at.
     * @param blueprint the matching blueprint.
     * @param settings the settings (mirror and rotation).
     * @param fancyPlacement if constructed or creative
     * @param pack the pack name.
     * @param path the path within the pack.
     * @return true if successful.
     */
    boolean setup(
      final ServerPlayer player,
      final Level world,
      final BlockPos pos,
      final Blueprint blueprint,
      final PlacementSettings settings,
      final boolean fancyPlacement,
      final String pack,
      final String path);
}
