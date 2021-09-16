package com.ldtteam.structurize.util;

import com.ldtteam.blockui.UiRenderMacros;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import java.util.List;

public class WorldRenderMacros extends UiRenderMacros
{
    private static final int MAX_DEBUG_TEXT_RENDER_DIST_SQUARED = 8 * 8 * 16;
    public static final RenderType GLINT_LINES = RenderTypes.LINES_GLINT;
    public static final RenderType NORMAL_LINES = RenderTypes.TRIANGLES_POS_COLOR;

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderRedGlintLineBox(final BufferSource buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final float lineWidth)
    {
        renderLineBox(buffer.getBuffer(GLINT_LINES), ps, posA, posB, 0xff, 0x0, 0x0, 0xff, lineWidth);
    }

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderWhiteLineBox(final BufferSource buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final float lineWidth)
    {
        renderLineBox(buffer.getBuffer(NORMAL_LINES), ps, posA, posB, 0xff, 0xff, 0xff, 0xff, lineWidth);
    }

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderLineBox(final VertexConsumer buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final int argbColor)
    {
        renderLineBox(buffer,
            ps,
            posA,
            posB,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            0.01f);
    }

    /**
     * Render a box around two positions
     *
     * @param posA First position
     * @param posB Second position
     */
    public static void renderLineBox(final VertexConsumer buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        final float halfLine = lineWidth / 2.0f;
        final float minX = Math.min(posA.getX(), posB.getX()) - halfLine;
        final float minY = Math.min(posA.getY(), posB.getY()) - halfLine;
        final float minZ = Math.min(posA.getZ(), posB.getZ()) - halfLine;
        final float minX2 = minX + lineWidth;
        final float minY2 = minY + lineWidth;
        final float minZ2 = minZ + lineWidth;

        final float maxX = Math.max(posA.getX(), posB.getX()) + 1 + halfLine;
        final float maxY = Math.max(posA.getY(), posB.getY()) + 1 + halfLine;
        final float maxZ = Math.max(posA.getZ(), posB.getZ()) + 1 + halfLine;
        final float maxX2 = maxX - lineWidth;
        final float maxY2 = maxY - lineWidth;
        final float maxZ2 = maxZ - lineWidth;

        final Matrix4f m = ps.last().pose();
        buffer.defaultColor(red, green, blue, alpha);

        populateRenderLineBox(minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2, m, buffer);

        buffer.unsetDefaultColor();
    }

    // TODO: ebo this, does vanilla have any ebo things?
    public static void populateRenderLineBox(final float minX,
        final float minY,
        final float minZ,
        final float minX2,
        final float minY2,
        final float minZ2,
        final float maxX,
        final float maxY,
        final float maxZ,
        final float maxX2,
        final float maxY2,
        final float maxZ2,
        final Matrix4f m,
        final VertexConsumer buf)
    {
        // z plane

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ).endVertex();
        buf.vertex(m, maxX, minY, minZ).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX2, minY2, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX2, maxY2, minZ).endVertex();
        buf.vertex(m, minX2, minY2, minZ).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX, maxY, minZ).endVertex();
        buf.vertex(m, minX2, maxY2, minZ).endVertex();

        buf.vertex(m, maxX, maxY, minZ).endVertex();
        buf.vertex(m, minX2, maxY2, minZ).endVertex();
        buf.vertex(m, minX, maxY, minZ).endVertex();

        buf.vertex(m, maxX, maxY, minZ).endVertex();
        buf.vertex(m, maxX2, maxY2, minZ).endVertex();
        buf.vertex(m, minX2, maxY2, minZ).endVertex();

        buf.vertex(m, maxX, maxY, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ).endVertex();
        buf.vertex(m, maxX2, maxY2, minZ).endVertex();

        buf.vertex(m, maxX, maxY, minZ).endVertex();
        buf.vertex(m, maxX, minY, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ).endVertex();

        //

        buf.vertex(m, minX, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX2, maxY2, minZ2).endVertex();

        buf.vertex(m, minX, maxY2, minZ2).endVertex();
        buf.vertex(m, minX, minY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY2, minZ2).endVertex();

        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY, minZ2).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();

        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();
        buf.vertex(m, maxX2, minY2, minZ2).endVertex();

        buf.vertex(m, maxX, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY2, minZ2).endVertex();

        buf.vertex(m, maxX, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX, minY2, minZ2).endVertex();

        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();
        buf.vertex(m, minX2, maxY, minZ2).endVertex();

        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();

        //

        buf.vertex(m, minX, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();

        buf.vertex(m, minX, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();

        buf.vertex(m, minX2, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY, maxZ2).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();

        buf.vertex(m, minX2, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY, maxZ2).endVertex();

        buf.vertex(m, maxX, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ2).endVertex();

        buf.vertex(m, maxX, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();

        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY, maxZ2).endVertex();

        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ2).endVertex();

        //

        buf.vertex(m, minX, minY, maxZ).endVertex();
        buf.vertex(m, maxX, minY, maxZ).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ).endVertex();

        buf.vertex(m, minX, minY, maxZ).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ).endVertex();
        buf.vertex(m, minX2, minY2, maxZ).endVertex();

        buf.vertex(m, minX, minY, maxZ).endVertex();
        buf.vertex(m, minX2, minY2, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ).endVertex();

        buf.vertex(m, minX, minY, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ).endVertex();
        buf.vertex(m, minX, maxY, maxZ).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, minX, maxY, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ).endVertex();
        buf.vertex(m, maxX, minY, maxZ).endVertex();

        // x plane

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX, minY, maxZ).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();
        buf.vertex(m, minX, minY2, minZ2).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX, minY2, minZ2).endVertex();
        buf.vertex(m, minX, maxY2, minZ2).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX, maxY2, minZ2).endVertex();
        buf.vertex(m, minX, maxY, minZ).endVertex();

        buf.vertex(m, minX, maxY, maxZ).endVertex();
        buf.vertex(m, minX, maxY, minZ).endVertex();
        buf.vertex(m, minX, maxY2, minZ2).endVertex();

        buf.vertex(m, minX, maxY, maxZ).endVertex();
        buf.vertex(m, minX, maxY2, minZ2).endVertex();
        buf.vertex(m, minX, maxY2, maxZ2).endVertex();

        buf.vertex(m, minX, maxY, maxZ).endVertex();
        buf.vertex(m, minX, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();

        buf.vertex(m, minX, maxY, maxZ).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();
        buf.vertex(m, minX, minY, maxZ).endVertex();

        //

        buf.vertex(m, minX2, maxY2, minZ).endVertex();
        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY2, minZ2).endVertex();

        buf.vertex(m, minX2, maxY2, minZ).endVertex();
        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY2, minZ).endVertex();

        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();
        buf.vertex(m, minX2, minY, minZ2).endVertex();

        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();

        buf.vertex(m, minX2, maxY2, maxZ).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();

        buf.vertex(m, minX2, maxY2, maxZ).endVertex();
        buf.vertex(m, minX2, minY2, maxZ).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();

        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, maxY, minZ2).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();

        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();

        //

        buf.vertex(m, maxX2, maxY2, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ).endVertex();
        buf.vertex(m, maxX2, minY2, minZ2).endVertex();

        buf.vertex(m, maxX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();
        buf.vertex(m, maxX2, minY, maxZ2).endVertex();

        buf.vertex(m, maxX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, maxZ).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, maxZ).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ).endVertex();

        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY, maxZ2).endVertex();

        //

        buf.vertex(m, maxX, minY, minZ).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX, minY, maxZ).endVertex();

        buf.vertex(m, maxX, minY, minZ).endVertex();
        buf.vertex(m, maxX, minY2, minZ2).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();

        buf.vertex(m, maxX, minY, minZ).endVertex();
        buf.vertex(m, maxX, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX, minY2, minZ2).endVertex();

        buf.vertex(m, maxX, minY, minZ).endVertex();
        buf.vertex(m, maxX, maxY, minZ).endVertex();
        buf.vertex(m, maxX, maxY2, minZ2).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX, maxY, minZ).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX, maxY2, minZ2).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX, maxY2, maxZ2).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX, minY, maxZ).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();

        // y plane

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();
        buf.vertex(m, minX, minY, maxZ).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, minX2, minY, minZ2).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();
        buf.vertex(m, minX2, minY, minZ2).endVertex();

        buf.vertex(m, minX, minY, minZ).endVertex();
        buf.vertex(m, maxX, minY, minZ).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();

        buf.vertex(m, maxX, minY, maxZ).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();
        buf.vertex(m, maxX, minY, minZ).endVertex();

        buf.vertex(m, maxX, minY, maxZ).endVertex();
        buf.vertex(m, maxX2, minY, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY, minZ2).endVertex();

        buf.vertex(m, maxX, minY, maxZ).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();
        buf.vertex(m, maxX2, minY, maxZ2).endVertex();

        buf.vertex(m, maxX, minY, maxZ).endVertex();
        buf.vertex(m, minX, minY, maxZ).endVertex();
        buf.vertex(m, minX2, minY, maxZ2).endVertex();

        //

        buf.vertex(m, maxX2, minY2, minZ).endVertex();
        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY2, minZ2).endVertex();

        buf.vertex(m, maxX2, minY2, minZ).endVertex();
        buf.vertex(m, minX2, minY2, minZ).endVertex();
        buf.vertex(m, minX2, minY2, minZ2).endVertex();

        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX, minY2, minZ2).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();

        buf.vertex(m, minX2, minY2, minZ2).endVertex();
        buf.vertex(m, minX, minY2, maxZ2).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, minY2, maxZ).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, minY2, maxZ).endVertex();
        buf.vertex(m, minX2, minY2, maxZ2).endVertex();
        buf.vertex(m, minX2, minY2, maxZ).endVertex();

        buf.vertex(m, maxX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX, minY2, minZ2).endVertex();

        buf.vertex(m, maxX2, minY2, minZ2).endVertex();
        buf.vertex(m, maxX2, minY2, maxZ2).endVertex();
        buf.vertex(m, maxX, minY2, maxZ2).endVertex();

        //

        buf.vertex(m, maxX2, maxY2, minZ).endVertex();
        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, maxY2, minZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, minZ).endVertex();
        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, maxY2, minZ).endVertex();

        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX, maxY2, minZ2).endVertex();

        buf.vertex(m, minX2, maxY2, minZ2).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, minX, maxY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ).endVertex();
        buf.vertex(m, minX2, maxY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX, maxY2, maxZ2).endVertex();

        buf.vertex(m, maxX2, maxY2, minZ2).endVertex();
        buf.vertex(m, maxX, maxY2, maxZ2).endVertex();
        buf.vertex(m, maxX2, maxY2, maxZ2).endVertex();

        //

        buf.vertex(m, minX, maxY, minZ).endVertex();
        buf.vertex(m, minX, maxY, maxZ).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();

        buf.vertex(m, minX, maxY, minZ).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();
        buf.vertex(m, minX2, maxY, minZ2).endVertex();

        buf.vertex(m, minX, maxY, minZ).endVertex();
        buf.vertex(m, minX2, maxY, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();

        buf.vertex(m, minX, maxY, minZ).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();
        buf.vertex(m, maxX, maxY, minZ).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX, maxY, minZ).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX2, maxY, minZ2).endVertex();
        buf.vertex(m, maxX2, maxY, maxZ2).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, maxX2, maxY, maxZ2).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();

        buf.vertex(m, maxX, maxY, maxZ).endVertex();
        buf.vertex(m, minX2, maxY, maxZ2).endVertex();
        buf.vertex(m, minX, maxY, maxZ).endVertex();
    }

    /**
     * Renders the given list of strings, 3 elements a row.
     *
     * @param pos                     position to render at
     * @param text                    text list
     * @param matrixStack             stack to use
     * @param buffer                  render buffer
     * @param forceWhite              force white for no depth rendering
     * @param mergeEveryXListElements merge every X elements of text list using a tostring call
     */
    public static void renderDebugText(final BlockPos pos,
        final List<String> text,
        final PoseStack matrixStack,
        final boolean forceWhite,
        final int mergeEveryXListElements,
        final MultiBufferSource buffer)
    {
        if (mergeEveryXListElements < 1)
        {
            throw new IllegalArgumentException("mergeEveryXListElements is less than 1");
        }

        final EntityRenderDispatcher erm = Minecraft.getInstance().getEntityRenderDispatcher();
        final int cap = text.size();
        if (cap > 0 && erm.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= MAX_DEBUG_TEXT_RENDER_DIST_SQUARED)
        {
            final Font fontrenderer = Minecraft.getInstance().font;

            matrixStack.pushPose();
            matrixStack.translate(pos.getX() + 0.5d, pos.getY() + 0.75d, pos.getZ() + 0.5d);
            matrixStack.mulPose(erm.cameraOrientation());
            matrixStack.scale(-0.014f, -0.014f, 0.014f);
            matrixStack.translate(0.0d, 18.0d, 0.0d);

            final float backgroundTextOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            final int alphaMask = (int) (backgroundTextOpacity * 255.0F) << 24;

            final Matrix4f rawPosMatrix = matrixStack.last().pose();

            for (int i = 0; i < cap; i += mergeEveryXListElements)
            {
                final TextComponent renderText = new TextComponent(
                    mergeEveryXListElements == 1 ? text.get(i) : text.subList(i, Math.min(i + mergeEveryXListElements, cap)).toString());
                final float textCenterShift = (float) (-fontrenderer.width(renderText) / 2);

                fontrenderer.drawInBatch(renderText,
                    textCenterShift,
                    0,
                    forceWhite ? 0xffffffff : 0x20ffffff,
                    false,
                    rawPosMatrix,
                    buffer,
                    true,
                    alphaMask,
                    0x00f000f0);
                if (!forceWhite)
                {
                    fontrenderer.drawInBatch(renderText, textCenterShift, 0, 0xffffffff, false, rawPosMatrix, buffer, false, 0, 0x00f000f0);
                }
                matrixStack.translate(0.0d, fontrenderer.lineHeight + 1, 0.0d);
            }

            matrixStack.popPose();
        }
    }

    private static final class RenderTypes extends RenderType
    {
        private RenderTypes(final String nameIn,
            final VertexFormat formatIn,
            final VertexFormat.Mode drawModeIn,
            final int bufferSizeIn,
            final boolean useDelegateIn,
            final boolean needsSortingIn,
            final Runnable setupTaskIn,
            final Runnable clearTaskIn)
        {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
            throw new IllegalStateException();
        }

        private static final RenderType LINES_GLINT = create("structurize_lines_glint",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            16384,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setLayeringState(NO_LAYERING)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(CULL)
                .setDepthTestState(NO_DEPTH_TEST)
                .createCompositeState(false));

        private static final RenderType TRIANGLES_POS_COLOR = create("structurize_normal_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            16384,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_SHADER)
                .setLayeringState(NO_LAYERING)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(CULL)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .createCompositeState(false));
    }
}
