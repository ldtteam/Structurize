package com.ldtteam.structurize.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Helper class to render boxes.
 */
public class BoxRenderer
{
    public static void drawSelectionBoundingBox(final Matrix4f matrix4f, AxisAlignedBB box, float red, float green, float blue, float alpha)
    {
        drawBoundingBox(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, red, green, blue, alpha);
    }

    public static void drawBoundingBox(final Matrix4f matrix4f, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, matrix4f);
        tessellator.draw();
    }

    public static void drawBoundingBox(
      BufferBuilder buffer,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      float red,
      float green,
      float blue,
      float alpha,
    final Matrix4f matrix)
    {
        //lower base
        buffer.func_227888_a_(matrix, minX, minY, minZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_227888_a_(matrix, minX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, maxX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, maxX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, minX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, minX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();

        //first leg
        buffer.func_227888_a_(matrix, minX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();

        //upper base
        buffer.func_227888_a_(matrix, maxX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, maxX, maxY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, minX, maxY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, minX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();

        //links runter
        buffer.func_227888_a_(matrix, minX, maxY, maxZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_227888_a_(matrix, minX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, maxX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();

        //rechts runter
        buffer.func_227888_a_(matrix, maxX, maxY, maxZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_227888_a_(matrix, maxX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_227888_a_(matrix, maxX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
    }

    /**
     * Private constructor to hide implicit one.
     */
    private BoxRenderer()
    {
        /*
         * Intentionally left empty.
         */
    }
}
