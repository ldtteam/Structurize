package com.ldtteam.structurize.blocks.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Interface for block entities that are blueprint anchors and have specific requirements to be placed.
 */
public interface IRequirementsBlueprintAnchorBlock
{
    /**
     * Get the list of requirements as chat components.
     * @return List for display.
     */
    List<MutableComponent> getRequirements(final Level level, final BlockPos pos, final Player player);

    /**
     * Check if the requirements are met for:
     * @param level the world.
     * @param pos the position.
     * @param player the player.
     * @return true if so.
     */
    boolean areRequirementsMet(final Level level, final BlockPos pos, final Player player);
}
