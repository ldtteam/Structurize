package com.ldtteam.structurize.client;

import com.ldtteam.structurize.items.ItemStackTooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

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
        font.drawInBatch(this.component.getStack().getHoverName(), x + 20, y + (20 - font.lineHeight) / 2f, 0xffffffff, false, pose, buffers, false, 0, 0x00f000f0);
    }

    @Override
    public void renderImage(@NotNull Font font, final int x, final int y,
                            @NotNull PoseStack poseStack,
                            @NotNull final ItemRenderer renderer,
                            final int z)
    {
        renderer.renderAndDecorateItem(this.component.getStack(), x + 2, y + 2);
    }
}
