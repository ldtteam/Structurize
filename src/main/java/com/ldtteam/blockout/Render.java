package com.ldtteam.blockout;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * Render utility functions.
 */
public final class Render
{
    private Render()
    {
        // Hide default constructor
    }

    /**
     * Draw an outlined untextured rectangle.
     * 
     * @param color argb
     */
    public static void drawOutlineRect(final MatrixStack ms, final int x, final int y, final int w, final int h, final int color)
    {
        drawOutlineRect(ms, x, y, w, h, color, 1.0f);
    }

    /**
     * Draw an outlined untextured rectangle.
     * 
     * @param color argb
     */
    public static void drawOutlineRect(final MatrixStack ms,
        final int x,
        final int y,
        final int w,
        final int h,
        final int color,
        final float lineWidth)
    {
        drawOutlineRect(ms.getLast()
            .getMatrix(), x, y, w, h, (color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, (color >> 24) & 0xff, lineWidth);
    }

    /**
     * Draw an outlined untextured rectangle.
     */
    public static void drawOutlineRect(final Matrix4f matrix,
        final int x,
        final int y,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        if (lineWidth <= 0.0F)
        {
            // If lineWidth is less than or equal to 0, a GL Error occurs
            return;
        }

        final BufferBuilder vertexBuffer = Tessellator.getInstance().getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        GL11.glLineWidth(lineWidth);
        vertexBuffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.pos(matrix, x, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(matrix, x + w, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(matrix, x + w, y + h, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.pos(matrix, x, y + h, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.finishDrawing();
        WorldVertexBufferUploader.draw(vertexBuffer);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
