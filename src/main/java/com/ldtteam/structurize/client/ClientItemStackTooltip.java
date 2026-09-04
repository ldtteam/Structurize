package com.ldtteam.structurize.client;

import com.ldtteam.structurize.items.ItemStackTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ClientItemStackTooltip implements ClientTooltipComponent
{
    private final ItemStackTooltip component;

    public ClientItemStackTooltip(@NotNull final ItemStackTooltip component)
    {
        this.component = component;
    }

    @Override
    public int getHeight(final Font font)
    {
        return 20;
    }

    @Override
    public int getWidth(@NotNull Font font)
    {
        return 20 + font.width(this.component.getStack().getDisplayName().getVisualOrderText());
    }

    @Override
    public void extractText(@NotNull final GuiGraphicsExtractor graphics,
        @NotNull final Font font,
        final int x,
        final int y)
    {
        graphics.text(font, this.component.getStack().getHoverName(), x + 20, y + (20 - font.lineHeight) / 2, 0xffffffff);
    }

    @Override
    public void extractImage(@NotNull final Font font,
        final int x,
        final int y,
        final int width,
        final int height,
        @NotNull final GuiGraphicsExtractor graphics)
    {
        graphics.item(this.component.getStack(), x + 2, y + 2);
        graphics.itemDecorations(getFont(this.component.getStack()), this.component.getStack(), x + 2, y + 2);
    }

    /**
     * @see com.ldtteam.blockui.BOGuiGraphics#getFont
     */
    private Font getFont(final ItemStack itemStack)
    {
        if (itemStack != null)
        {
            final Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
            if (font != null)
            {
                return font;
            }
        }
        return Minecraft.getInstance().font;
    }
}
