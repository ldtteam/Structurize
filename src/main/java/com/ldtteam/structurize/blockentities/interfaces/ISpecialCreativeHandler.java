package com.ldtteam.structurize.blockentities.interfaces;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.structure.AbstractStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Special creative placement handler anchors.
 */
public interface ISpecialCreativeHandler
{
    /**
     * Get the special structure handler.
     */
    AbstractStructureHandler getStructureHandler(final Level world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement);
}
