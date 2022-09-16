package com.ldtteam.structurize.api.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An item that supports shift-wheel-scrolling
 */
public interface IScrollableItem
{
    /**
     * Called first on client side when the player shift-scrolls with this item selected.
     * If that succeeds, called again on server side.
     * @param player the player
     * @param stack the item stack
     * @param delta the scroll delta; negative is up, positive is down
     * @return on client side, return SUCCESS to pass to server, FAIL to cancel, or PASS to do normal scrolling.
     *         on server side, return value is ignored.
     */
    InteractionResult onMouseScroll(@NotNull Player player, @NotNull ItemStack stack, double delta);
}
