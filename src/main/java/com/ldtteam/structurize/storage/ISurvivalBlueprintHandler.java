package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Handle the placement of blueprints in survival.
 */
public interface ISurvivalBlueprintHandler
{
    /**
     * Get the unique registry id of the handler.
     * @return the id.
     */
    String getId();

    /**
     * Get the display name of the handler.
     * @return the chat component.
     */
    Component getDisplayName();

    /**
     * Client side check if this handler can handle this blueprint.
     * @param blueprint the blueprint to check.
     * @param level the world.
     * @param player the player trying to place it.
     * @param pos the position they're trying to place it at.
     * @param placementSettings the placement settings.
     * @return true if so.
     */
    boolean canHandle(final Blueprint blueprint, final ClientLevel level, final Player player, final BlockPos pos, final PlacementSettings placementSettings);

    /**
     * Handle the placement.
     * @param blueprint the blueprint to place.
     * @param clientPack if this blueprint is not inside any server pack.
     * @param level the world.
     * @param player the player placing it.
     * @param pos the position they're placing it at.
     * @param placementSettings the placement settings.
     */
    void handle(final Blueprint blueprint, final boolean clientPack, final Level level, final Player player, final BlockPos pos, final PlacementSettings placementSettings);
}
