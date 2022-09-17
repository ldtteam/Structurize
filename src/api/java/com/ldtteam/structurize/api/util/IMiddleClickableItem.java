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
public interface IMiddleClickableItem
{
    /**
     * Called first on client side when the player middle-clicks with this item selected.
     * If that succeeds, called again on server side.
     * @param player the player
     * @param stack the item stack
     * @param pos the clicked block, or null if clicking air
     * @param modifiers GLFW modifier keys held
     * @return on client side, return SUCCESS to pass to server, FAIL to cancel, or PASS to do normal scrolling.
     *         on server side, return value is ignored.
     */
    @NotNull
    InteractionResult onMiddleClick(@NotNull Player player, @NotNull ItemStack stack, @Nullable BlockPos pos, int modifiers);
}
