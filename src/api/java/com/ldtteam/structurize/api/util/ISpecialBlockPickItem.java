package com.ldtteam.structurize.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An item that supports middle-clicking
 */
public interface ISpecialBlockPickItem
{
    /**
     * Called first on client side when the player presses "pick block" (default middle mouse) with this item selected.
     * If that succeeds, called again on server side.
     * @param player the player
     * @param stack the item stack
     * @param pos the focused block, or null if on air
     * @param ctrlKey ctrl key is pressed too
     * @return (client) return SUCCESS to send to server, FAIL to cancel, or PASS to do normal pick action.
     *         (server) return value is ignored.
     */
    @NotNull
    InteractionResult onBlockPick(@NotNull Player player, @NotNull ItemStack stack, @Nullable BlockPos pos, boolean ctrlKey);
}
