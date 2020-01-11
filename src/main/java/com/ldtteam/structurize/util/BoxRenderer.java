package com.ldtteam.structurize.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * Helper class to render boxes.
 */
public class BoxRenderer
{
    public static void drawSelectionBoundingBox(AxisAlignedBB box, float red, float green, float blue, float alpha)
    {
        drawBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
    }

    public static void drawBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        drawBoundingBox(bufferbuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        tessellator.draw();
    }

    public static void drawBoundingBox(
      BufferBuilder buffer,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      float red,
      float green,
      float blue,
      float alpha)
    {
        buffer.func_225582_a_(minX, minY, minZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_225582_a_(minX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(minX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(minX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(minX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, maxY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(minX, maxY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(minX, maxY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(minX, maxY, maxZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_225582_a_(minX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, maxY, maxZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_225582_a_(maxX, minY, maxZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, maxY, minZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
        buffer.func_225582_a_(maxX, minY, minZ).func_227885_a_(red, green, blue, alpha).endVertex();
        buffer.func_225582_a_(maxX, minY, minZ).func_227885_a_(red, green, blue, 0.0F).endVertex();
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
