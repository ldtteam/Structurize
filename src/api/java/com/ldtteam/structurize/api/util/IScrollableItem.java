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
     * @param deltaX the scroll delta; negative is up, positive is down
     * @param deltaY the scroll delta; negative is up, positive is down
     * @param ctrlKey the ctrl key is held
     * @return (client) return SUCCESS to pass to server, FAIL to cancel, or PASS to do normal scrolling.
     *         (server) return value is ignored.
     */
    @NotNull
    InteractionResult onMouseScroll(@NotNull Player player, @NotNull ItemStack stack, double deltaX, double deltaY, boolean ctrlKey);
}
