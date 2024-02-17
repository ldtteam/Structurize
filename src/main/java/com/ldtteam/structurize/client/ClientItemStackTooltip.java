package com.ldtteam.structurize.client;

import com.ldtteam.structurize.items.ItemStackTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class ClientItemStackTooltip implements ClientTooltipComponent
{
    private final ItemStackTooltip component;

    public ClientItemStackTooltip(@NotNull final ItemStackTooltip component)
    {
        this.component = component;
    }

    @Override
    public int getHeight()
    {
        return 20;
    }

    @Override
    public int getWidth(@NotNull Font font)
    {
        return 20 + font.width(this.component.getStack().getDisplayName().getVisualOrderText());
    }

    @Override
    public void renderText(@NotNull Font font, final int x, final int y,
                           @NotNull final Matrix4f pose,
                           @NotNull final MultiBufferSource.BufferSource buffers)
    {
        font.drawInBatch(this.component.getStack().getHoverName(), x + 20, y + (20 - font.lineHeight) / 2f, 0xffffffff, false, pose, buffers, Font.DisplayMode.NORMAL, 0, 0x00f000f0);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics target)
    {
        target.renderItem(this.component.getStack(), x + 2, y + 2);
        target.renderItemDecorations(getFont(this.component.getStack()), this.component.getStack(), x + 2, y + 2);
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
