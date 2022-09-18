package com.ldtteam.structurize.items;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A tooltip component that renders an {@link ItemStack}
 */
public class ItemStackTooltip implements TooltipComponent
{
    private final ItemStack stack;

    public ItemStackTooltip(@NotNull final ItemStack stack)
    {
        this.stack = stack;
    }

    @NotNull
    public ItemStack getStack()
    {
        return this.stack;
    }
}
