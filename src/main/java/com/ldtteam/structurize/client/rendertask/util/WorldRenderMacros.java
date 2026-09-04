package com.ldtteam.structurize.client.rendertask.util;

import com.ldtteam.blockui.UiRenderMacros;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.ldtteam.structurize.client.rendertask.util.BufferSourceCompat;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gizmos.TextGizmo;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.LinkedList;
import java.util.List;

public class WorldRenderMacros extends UiRenderMacros
{
    private static final int MAX_DEBUG_TEXT_RENDER_DIST_SQUARED = 8 * 8 * 16;
    public static final RenderType LINES = RenderTypes.LINES;
    public static final RenderType LINES_WITH_WIDTH = RenderTypes.LINES_WITH_WIDTH;
    public static final RenderType GLINT_LINES = RenderTypes.GLINT_LINES;
    public static final RenderType GLINT_LINES_WITH_WIDTH = RenderTypes.GLINT_LINES_WITH_WIDTH;
    public static final RenderType COLORED_TRIANGLES = RenderTypes.COLORED_TRIANGLES;
    public static final RenderType COLORED_TRIANGLES_NC_ND = RenderTypes.COLORED_TRIANGLES_NC_ND;

    private static final LinkedList<RenderType> buffers = new LinkedList<>();
    /**
     * Always use {@link #getBufferSource} when actually using the buffer source
     */
    private static BufferSourceCompat bufferSource;

    /**
     * Put type at the first position.
     *
     * @param bufferType type to put in
     */
    public static void putBufferHead(final RenderType bufferType)
    {
        buffers.addFirst(bufferType);
        bufferSource = null;
    }

    /**
     * Put type at the last position.
     *
     * @param bufferType type to put in
     */
    public static void putBufferTail(final RenderType bufferType)
    {
        buffers.addLast(bufferType);
        bufferSource = null;
    }

    /**
     * Put type before the given buffer or if not found then at first position.
     *
     * @param bufferType type to put in
     * @param putBefore  search for type to put before
     */
    public static void putBufferBefore(final RenderType bufferType, final RenderType putBefore)
    {
        buffers.add(Math.max(0, buffers.indexOf(putBefore)), bufferType);
        bufferSource = null;
    }

    /**
     * Put type after the given buffer or if not found then at last position.
     *
     * @param bufferType type to put in
     * @param putAfter   search for type to put after
     */
    public static void putBufferAfter(final RenderType bufferType, final RenderType putAfter)
    {
        final int index = buffers.indexOf(putAfter);
        if (index == -1)
        {
            buffers.add(bufferType);
        }
        else
        {
            buffers.add(index + 1, bufferType);
        }
        bufferSource = null;
    }

    static
    {
        putBufferTail(WorldRenderMacros.COLORED_TRIANGLES);
        putBufferTail(WorldRenderMacros.LINES);
        putBufferTail(WorldRenderMacros.LINES_WITH_WIDTH);
        putBufferTail(WorldRenderMacros.GLINT_LINES);
        putBufferTail(WorldRenderMacros.GLINT_LINES_WITH_WIDTH);
        putBufferTail(WorldRenderMacros.COLORED_TRIANGLES_NC_ND);
    }

    public static BufferSourceCompat getBufferSource()
    {
        if (bufferSource == null)
        {
            bufferSource = new BufferSourceCompat();
        }
        return bufferSource;
    }

    /**
     * Render a black box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderBlackLineBox(final BufferSourceCompat buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final float lineWidth)
    {
        renderLineBox(buffer.getBuffer(LINES_WITH_WIDTH), ps, posA, posB, 0x00, 0x00, 0x00, 0xff, lineWidth);
    }

    /**
     * Render a red glint box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderRedGlintLineBox(final BufferSourceCompat buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final float lineWidth)
    {
        renderLineBox(buffer.getBuffer(GLINT_LINES_WITH_WIDTH), ps, posA, posB, 0xff, 0x0, 0x0, 0xff, lineWidth);
    }

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderWhiteLineBox(final BufferSourceCompat buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final float lineWidth)
    {
        renderLineBox(buffer.getBuffer(LINES_WITH_WIDTH), ps, posA, posB, 0xff, 0xff, 0xff, 0xff, lineWidth);
    }

    /**
     * Render a colored box around from aabb
     *
     * @param aabb the box
     */
    public static void renderLineAABB(final VertexConsumer buffer,
        final PoseStack ps,
        final AABB aabb,
        final int argbColor,
        final float lineWidth)
    {
        renderLineAABB(buffer,
            ps,
            aabb,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            lineWidth);
    }

    /**
     * Render a colored box around from aabb
     *
     * @param aabb the box
     */
    public static void renderLineAABB(final VertexConsumer buffer,
        final PoseStack ps,
        final AABB aabb,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        renderLineBox(buffer,
            ps,
            (float) aabb.minX,
            (float) aabb.minY,
            (float) aabb.minZ,
            (float) aabb.maxX,
            (float) aabb.maxY,
            (float) aabb.maxZ,
            red,
            green,
            blue,
            alpha,
            lineWidth);
    }

    /**
     * Render a colored box around position
     *
     * @param pos The Position
     */
    public static void renderLineBox(final VertexConsumer buffer,
        final PoseStack ps,
        final BlockPos pos,
        final int argbColor,
        final float lineWidth)
    {
        renderLineBox(buffer,
            ps,
            pos,
            pos,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            lineWidth);
    }

    /**
     * Render a colored box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderLineBox(final VertexConsumer buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final int argbColor,
        final float lineWidth)
    {
        renderLineBox(buffer,
            ps,
            posA,
            posB,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            lineWidth);
    }

    /**
     * Render a box around two positions
     *
     * @param posA First position.
     * @param posB Second position.
     * @param red Red component.
     * @param green Green component.
     * @param blue Blue component.
     * @param alpha Alpha component.
     * @param lineWidth Line width.
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
        renderLineBox(buffer,
            ps,
            Math.min(posA.getX(), posB.getX()),
            Math.min(posA.getY(), posB.getY()),
            Math.min(posA.getZ(), posB.getZ()),
            Math.max(posA.getX(), posB.getX()) + 1,
            Math.max(posA.getY(), posB.getY()) + 1,
            Math.max(posA.getZ(), posB.getZ()) + 1,
            red,
            green,
            blue,
            alpha,
            lineWidth);
    }

    /**
     * Render a box around two positions
     *
     * @param minX Minimum X coordinate.
     * @param minY Minimum Y coordinate.
     * @param minZ Minimum Z coordinate.
     * @param maxX Maximum X coordinate.
     * @param maxY Maximum Y coordinate.
     * @param maxZ Maximum Z coordinate.
     */
    public static void renderLineBox(final VertexConsumer buffer,
        final PoseStack ps,
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        if (alpha == 0)
        {
            return;
        }

        final float halfLine = lineWidth / 2.0f;
        minX -= halfLine;
        minY -= halfLine;
        minZ -= halfLine;
        final float minX2 = minX + lineWidth;
        final float minY2 = minY + lineWidth;
        final float minZ2 = minZ + lineWidth;

        maxX += halfLine;
        maxY += halfLine;
        maxZ += halfLine;
        final float maxX2 = maxX - lineWidth;
        final float maxY2 = maxY - lineWidth;
        final float maxZ2 = maxZ - lineWidth;

        final Matrix4f m = ps.last().pose();
        populateRenderLineBox(
            minX,
            minY,
            minZ,
            minX2,
            minY2,
            minZ2,
            maxX,
            maxY,
            maxZ,
            maxX2,
            maxY2,
            maxZ2,
            (alpha << 24) | (red << 16) | (green << 8) | blue,
            m,
            buffer);
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
        final int argbColor,
        final Matrix4f m,
        final VertexConsumer buf)
    {
        // z plane

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);

        //

        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);

        //

        buf.addVertex(m, minX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(argbColor);

        //

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);

        // x plane

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);

        //

        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);

        //

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(argbColor);

        //

        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);

        // y plane

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(argbColor);

        //

        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(argbColor);

        //

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(argbColor);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(argbColor);

        //

        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);

        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(argbColor);
        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
    }

    public static void renderBox(final BufferSourceCompat buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final int argbColor)
    {
        renderBox(buffer.getBuffer(COLORED_TRIANGLES),
            ps,
            posA,
            posB,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff);
    }

    public static void renderBox(final VertexConsumer buffer,
        final PoseStack ps,
        final BlockPos posA,
        final BlockPos posB,
        final int red,
        final int green,
        final int blue,
        final int alpha)
    {
        if (alpha == 0)
        {
            return;
        }

        final float minX = Math.min(posA.getX(), posB.getX());
        final float minY = Math.min(posA.getY(), posB.getY());
        final float minZ = Math.min(posA.getZ(), posB.getZ());

        final float maxX = Math.max(posA.getX(), posB.getX()) + 1;
        final float maxY = Math.max(posA.getY(), posB.getY()) + 1;
        final float maxZ = Math.max(posA.getZ(), posB.getZ()) + 1;

        final Matrix4f m = ps.last().pose();
        final int argbColor = (alpha << 24) | (red << 16) | (green << 8) | blue;

        populateCuboid(minX, minY, minZ, maxX, maxY, maxZ, argbColor, m, buffer);
    }

    public static void populateCuboid(final float minX,
        final float minY,
        final float minZ,
        final float maxX,
        final float maxY,
        final float maxZ,
        final int argbColor,
        final Matrix4f m,
        final VertexConsumer buf)
    {
        // z plane

        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);

        // y plane

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);

        // x plane

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, minX, minY, minZ).setColor(argbColor);

        buf.addVertex(m, minX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, maxZ).setColor(argbColor);
        buf.addVertex(m, minX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, minY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);

        buf.addVertex(m, maxX, minY, maxZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, minZ).setColor(argbColor);
        buf.addVertex(m, maxX, maxY, maxZ).setColor(argbColor);
    }

    public static void renderFillRectangle(final BufferSourceCompat buffer,
        final PoseStack ps,
        final int x,
        final int y,
        final int z,
        final int w,
        final int h,
        final int argbColor)
    {
        populateRectangle(x,
            y,
            z,
            w,
            h,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            buffer.getBuffer(COLORED_TRIANGLES_NC_ND),
            ps.last().pose());
    }

    public static void populateRectangle(final int x,
        final int y,
        final int z,
        final int w,
        final int h,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final VertexConsumer buffer,
        final Matrix4f m)
    {
        if (alpha == 0)
        {
            return;
        }

        final int argbColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        
        buffer.addVertex(m, x, y, z).setColor(argbColor);
        buffer.addVertex(m, x, y + h, z).setColor(argbColor);
        buffer.addVertex(m, x + w, y + h, z).setColor(argbColor);
        
        buffer.addVertex(m, x, y, z).setColor(argbColor);
        buffer.addVertex(m, x + w, y + h, z).setColor(argbColor);
        buffer.addVertex(m, x + w, y, z).setColor(argbColor);
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
        final BufferSourceCompat buffer)
    {
        renderDebugText(pos, pos, text, matrixStack, forceWhite, mergeEveryXListElements, buffer);
    }

    /**
     * Renders the given list of strings, 3 elements a row.
     *
     * @param renderPos               position to render at
     * @param worldPos                (logic) position in world
     * @param text                    text list
     * @param matrixStack             stack to use
     * @param buffer                  render buffer
     * @param forceWhite              force white for no depth rendering
     * @param mergeEveryXListElements merge every X elements of text list using a tostring call
     */
    @SuppressWarnings("resource")
    public static void renderDebugText(final BlockPos renderPos,
        final BlockPos worldPos,
        final List<String> text,
        final PoseStack matrixStack,
        final boolean forceWhite,
        final int mergeEveryXListElements,
        final BufferSourceCompat buffer)
    {
        if (mergeEveryXListElements < 1)
        {
            throw new IllegalArgumentException("mergeEveryXListElements is less than 1");
        }

        final EntityRenderDispatcher erm = Minecraft.getInstance().getEntityRenderDispatcher();
        final int cap = text.size();
        if (cap > 0 && Minecraft.getInstance().gameRenderer.mainCamera().position().distanceToSqr(worldPos.getX(), worldPos.getY(), worldPos.getZ()) <= MAX_DEBUG_TEXT_RENDER_DIST_SQUARED)
        {
            final Font fontrenderer = Minecraft.getInstance().font;

            matrixStack.pushPose();
            matrixStack.translate(renderPos.getX() + 0.5d, renderPos.getY() + 0.6d, renderPos.getZ() + 0.5d);
            matrixStack.mulPose(erm.camera.rotation());
            matrixStack.scale(-0.014f, -0.014f, 0.014f);

            final float backgroundTextOpacity = 0f;
            final int alphaMask = (int) (backgroundTextOpacity * 255.0F) << 24;

            final Matrix4f rawPosMatrix = matrixStack.last().pose();

            for (int i = 0; i < cap; i += mergeEveryXListElements)
            {
                final MutableComponent renderText = Component.literal(
                    mergeEveryXListElements == 1 ? text.get(i) : text.subList(i, Math.min(i + mergeEveryXListElements, cap)).toString());
                final float textCenterShift = (float) (-fontrenderer.width(renderText) / 2);

                final Vec3 textWorldPos = new Vec3(
                    renderPos.getX() + 0.5d,
                    renderPos.getY() + 0.6d + i * (fontrenderer.lineHeight + 1) * 0.014f,
                    renderPos.getZ() + 0.5d);

                Gizmos.billboardText(renderText.getString(),
                    textWorldPos,
                    TextGizmo.Style.forColorAndCentered(forceWhite ? 0xffffffff : 0x20ffffff));
                if (!forceWhite)
                {
                    Gizmos.billboardText(renderText.getString(),
                        textWorldPos,
                        TextGizmo.Style.forColorAndCentered(0xffffffff));
                }
            }

            matrixStack.popPose();
        }
    }

    /**
     * Render a wireframe box.
     *
     * @param poseStack         pose stack
     * @param bufferSource      buffer source
     * @param bounds            bounding box to draw
     * @param width             line width
     * @param color             line color (ARGB)
     * @param showThroughBlocks true to render through existing blocks, false to only render in air
     */
    public static void renderLineBox(
        final PoseStack poseStack, final BufferSourceCompat bufferSource,
        final AABB bounds, final float width, final int color, final boolean showThroughBlocks)
    {
        final float halfLine = width / 2.0f;
        final float minX = (float) (bounds.minX - halfLine);
        final float minY = (float) (bounds.minY - halfLine);
        final float minZ = (float) (bounds.minZ - halfLine);
        final float minX2 = minX + width;
        final float minY2 = minY + width;
        final float minZ2 = minZ + width;

        final float maxX = (float) (bounds.maxX + halfLine);
        final float maxY = (float) (bounds.maxY + halfLine);
        final float maxZ = (float) (bounds.maxZ + halfLine);
        final float maxX2 = maxX - width;
        final float maxY2 = maxY - width;
        final float maxZ2 = maxZ - width;

        final int red = ARGB.red(color);
        final int green = ARGB.green(color);
        final int blue = ARGB.blue(color);
        final int alpha = ARGB.alpha(color);

        if (showThroughBlocks)
        {
            renderLineBox(poseStack, bufferSource.getBuffer(RenderTypes.LINES_INSIDE_BLOCKS),
                minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2,
                red / 2, green / 2, blue / 2, alpha / 2);
        }

        renderLineBox(poseStack, bufferSource.getBuffer(RenderTypes.LINES_OUTSIDE_BLOCKS),
            minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2,
            red, green, blue, alpha);
    }

    /**
     * Call after a series of {@link #renderLineBox(PoseStack, BufferSourceCompat, AABB, float, int, boolean)}
     *
     * @param bufferSource buffer source
     */
    public static void endRenderLineBox(final BufferSourceCompat bufferSource)
    {
        bufferSource.endBatch();
    }

    /**
     * Render a wireframe box.
     *
     * @param poseStack pose stack
     * @param buffer    buffer
     * @param minX      min X
     * @param minY      min Y
     * @param minZ      min Z
     * @param minX2     min X + width
     * @param minY2     min Y + width
     * @param minZ2     min Z + width
     * @param maxX      max X
     * @param maxY      max Y
     * @param maxZ      max Z
     * @param maxX2     max X - width
     * @param maxY2     max Y - width
     * @param maxZ2     max Z - width
     * @param red       red
     * @param green     green
     * @param blue      blue
     * @param alpha     alpha
     */
    private static void renderLineBox(
        final PoseStack poseStack, final VertexConsumer buffer,
        final float minX, final float minY, final float minZ,
        final float minX2, final float minY2, final float minZ2,
        final float maxX, final float maxY, final float maxZ,
        final float maxX2, final float maxY2, final float maxZ2,
        final int red, final int green, final int blue, final int alpha)
    {
        final int argbColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        WorldRenderMacros.populateRenderLineBox(
            minX,
            minY,
            minZ,
            minX2,
            minY2,
            minZ2,
            maxX,
            maxY,
            maxZ,
            maxX2,
            maxY2,
            maxZ2,
            argbColor,
            poseStack.last().pose(),
            buffer);
    }
}
