package com.ldtteam.structurize.blockentities.interfaces;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blueprint anchor that requires special paste handling.
 */
public interface ISpecialPasteBlueprintAnchorBlock
{
    /**
     * Special paste option.
     * @param blueprint the blueprint to paste.
     * @param level the world to paste it in.
     * @param player the player doing the pasting.
     * @param pos the position it's pasted at.
     * @param placementSettings the placement setting.
     * @param complete if a complete paste.
     * @param state the state of this block.
     */
    void paste(final Blueprint blueprint, final Level level, final Player player, final BlockPos pos, final PlacementSettings placementSettings, final boolean complete, final
      BlockState state);
}
