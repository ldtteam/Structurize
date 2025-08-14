package com.ldtteam.structurize.util;

import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL30C;

import java.util.Collection;
import java.util.List;

public abstract class WorldRenderMacros
{
    // 4 chunks squared
    public static final int MAX_DEBUG_TEXT_RENDER_DIST_SQUARED = Mth.square(4 * 16);
    public static final RenderType LINES = RenderTypes.LINES;
    public static final RenderType LINES_WITH_WIDTH = RenderTypes.LINES_WITH_WIDTH;
    public static final RenderType LINES_WITH_WIDTH_DEPTH_INVERT = RenderTypes.LINES_WITH_WIDTH_DEPTH_INVERT;
    public static final RenderType GLINT_LINES = RenderTypes.GLINT_LINES;
    public static final RenderType GLINT_LINES_WITH_WIDTH = RenderTypes.GLINT_LINES_WITH_WIDTH;
    public static final RenderType COLORED_TRIANGLES = RenderTypes.COLORED_TRIANGLES;
    public static final RenderType COLORED_TRIANGLES_NC_ND = RenderTypes.COLORED_TRIANGLES_NC_ND;
    public static final Stage STAGE_FOR_LINES = Stage.AFTER_ENTITIES;
    public static final float DEFAULT_LINE_WIDTH = 0.025f;

    public Minecraft mc;
    public RenderLevelStageEvent event;
    public LocalPlayer clientPlayer;
    public BufferSource bufferSource;
    public PoseStack poseStack;
    public DeltaTracker deltaTracker;
    public ClientLevel clientLevel;
    public ItemStack mainHandItem;
    public Vec3 cameraPosition;
    /**
     * In chunks
     */
    public int clientRenderDist;

    /**
     * Call this from event handler
     * 
     * @param event
     */
    public void renderWorldLastEvent(final RenderLevelStageEvent e)
    {
        mc = Minecraft.getInstance();
        event = e;
        clientPlayer = mc.player;
        if (clientPlayer == null) // server login phase
        {
            return;
        }

        bufferSource = mc.renderBuffers().bufferSource();
        poseStack = event.getPoseStack();
        deltaTracker = event.getPartialTick();
        clientLevel = mc.level;
        mainHandItem = clientPlayer.getMainHandItem();
        cameraPosition = event.getCamera().getPosition();
        clientRenderDist = mc.options.renderDistance().get();

        final Matrix4fStack mvMatrix = RenderSystem.getModelViewStack();
        mvMatrix.pushMatrix();
        mvMatrix.identity();
        mvMatrix.mul(event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();

        renderWithinContext(event.getStage());

        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * This is called with properly prepared context. Do here what you want
     * 
     * @param stage render world stage
     */
    protected abstract void renderWithinContext(Stage stage);

    /**
     * Moved pose context to camera and given pos
     * 
     * @see #popPose()
     */
    public final void pushPoseCameraToPos(final BlockPos pos)
    {
        poseStack.pushPose();
        poseStack.translate(pos.getX() - cameraPosition.x(), pos.getY() - cameraPosition.y(), pos.getZ() - cameraPosition.z());
    }

    public final void popPose()
    {
        poseStack.popPose();
    }

    public void pushShaderMvMatrixFromPose()
    {
        final Matrix4fStack mvMatrix = RenderSystem.getModelViewStack();
        mvMatrix.pushMatrix();
        mvMatrix.mul(poseStack.last().pose());
        RenderSystem.applyModelViewMatrix();
    }

    public void popShaderMvMatrix()
    {
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * @return true if given aabb can be in any way seen by camera
     */
    public final boolean isVisible(final AABB aabb)
    {
        return event.getFrustum().isVisible(aabb);
    }

    /**
     * @return true if given pos can be in any way seen by camera
     */
    public final boolean isVisible(final BlockPos pos)
    {
        return isVisible(pos, pos);
    }
    
    /**
     * @return true if given box can be in any way seen by camera
     */
    public final boolean isVisible(final BlockPos posA, final BlockPos posB)
    {
        return event.getFrustum()
            .cubeInFrustum(Math.min(posA.getX(), posB.getX()),
                Math.min(posA.getY(), posB.getY()),
                Math.min(posA.getZ(), posB.getZ()),
                Math.max(posA.getX(), posB.getX()) + 1,
                Math.max(posA.getY(), posB.getY()) + 1,
                Math.max(posA.getZ(), posB.getZ()) + 1);
    }

    /**
     * Draw a blueprint at given pos.
     *
     * @param previewData the blueprint and context to draw.
     * @param pos         position to render at
     */
    public final void renderBlueprint(final BlueprintPreviewData blueprint, final BlockPos pos)
    {
        BlueprintHandler.getInstance().draw(blueprint, pos, event);
    }

    /**
     * Draw a blueprint at list of given pos.
     *
     * @param previewData the blueprint and context to draw.
     * @param points      list of positions to render at
     */
    public final void renderBlueprint(final BlueprintPreviewData blueprint, final Collection<BlockPos> points)
    {
        BlueprintHandler.getInstance().drawAtListOfPositions(blueprint, points, event);
    }

    /**
     * Render a black box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public final void renderBlackLineBox(final BlockPos posA, final BlockPos posB, final float lineWidth)
    {
        renderLineBox(LINES_WITH_WIDTH, posA, posB, 0x00, 0x00, 0x00, 0xff, lineWidth);
    }

    /**
     * Render a red glint box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public final void renderRedGlintLineBox(final BlockPos posA, final BlockPos posB, final float lineWidth)
    {
        renderLineBox(GLINT_LINES_WITH_WIDTH, posA, posB, 0xff, 0x0, 0x0, 0xff, lineWidth);
    }

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public final void renderWhiteLineBox(final BlockPos posA, final BlockPos posB, final float lineWidth)
    {
        renderLineBox(LINES_WITH_WIDTH, posA, posB, 0xff, 0xff, 0xff, 0xff, lineWidth);
    }

    /**
     * Render a colored box around from aabb
     *
     * @param aabb the box
     */
    public final void renderLineAABB(final RenderType renderType, final AABB aabb, final int argbColor, final float lineWidth)
    {
        renderLineAABB(renderType,
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
    public final void renderLineAABB(final RenderType renderType,
        final AABB aabb,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        renderLineBox(renderType,
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
    public final void renderLineBox(final RenderType renderType,
        final BlockPos pos,
        final int argbColor,
        final float lineWidth)
    {
        renderLineBox(renderType,
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
    public final void renderLineBox(final RenderType renderType,
        final BlockPos posA,
        final BlockPos posB,
        final int argbColor,
        final float lineWidth)
    {
        renderLineBox(renderType,
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
     * @param posA First position
     * @param posB Second position
     */
    public final void renderLineBox(final RenderType renderType,
        final BlockPos posA,
        final BlockPos posB,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final float lineWidth)
    {
        renderLineBox(renderType,
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
     * @param posA First position
     * @param posB Second position
     */
    public final void renderLineBox(final RenderType renderType,
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

        populateRenderLineBox(minX, minY, minZ, minX2, minY2, minZ2, maxX, maxY, maxZ, maxX2, maxY2, maxZ2, red, green, blue, alpha, poseStack.last().pose(), bufferSource.getBuffer(renderType));
    }

    // TODO: ebo this, does vanilla have any ebo things?
    protected final void populateRenderLineBox(final float minX,
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
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final Matrix4f m,
        final VertexConsumer buf)
    {
        // z plane

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, minX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);

        // x plane

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);

        // y plane

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY, maxZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, minY2, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, minY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, minY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY2, maxZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX2, maxY2, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY2, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY2, maxZ2).setColor(red, green, blue, alpha);

        //

        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, minZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX2, maxY, maxZ2).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
    }

    public final void renderBox(final RenderType renderType,
        final BlockPos posA,
        final BlockPos posB,
        final int argbColor)
    {
        renderBox(renderType,
            posA,
            posB,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff);
    }

    public final void renderBox(final RenderType renderType,
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

        populateCuboid(minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, poseStack.last().pose(), bufferSource.getBuffer(renderType));
    }

    protected final void populateCuboid(final float minX,
        final float minY,
        final float minZ,
        final float maxX,
        final float maxY,
        final float maxZ,
        final int red,
        final int green,
        final int blue,
        final int alpha,
        final Matrix4f m,
        final VertexConsumer buf)
    {
        // z plane

        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        // y plane

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        // x plane

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, minY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        buf.addVertex(m, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buf.addVertex(m, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
    }

    public final void renderFillRectangle(final int x,
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
            bufferSource.getBuffer(COLORED_TRIANGLES_NC_ND),
            poseStack.last().pose());
    }

    protected final void populateRectangle(final int x,
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

        buffer.addVertex(m, x, y, z).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x, y + h, z).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y + h, z).setColor(red, green, blue, alpha);
        
        buffer.addVertex(m, x, y, z).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y + h, z).setColor(red, green, blue, alpha);
        buffer.addVertex(m, x + w, y, z).setColor(red, green, blue, alpha);
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
    public final void renderDebugText(final BlockPos pos,
        final List<String> text,
        final boolean forceWhite,
        final int mergeEveryXListElements)
    {
        renderDebugText(pos, pos, text, forceWhite, mergeEveryXListElements);
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
    public final void renderDebugText(final BlockPos renderPos,
        final BlockPos worldPos,
        final List<String> text,
        final boolean forceWhite,
        final int mergeEveryXListElements)
    {
        if (mergeEveryXListElements < 1)
        {
            throw new IllegalArgumentException("mergeEveryXListElements is less than 1");
        }

        final EntityRenderDispatcher erm = Minecraft.getInstance().getEntityRenderDispatcher();
        final int cap = text.size();
        if (cap > 0 && erm.distanceToSqr(worldPos.getX(), worldPos.getY(), worldPos.getZ()) <= MAX_DEBUG_TEXT_RENDER_DIST_SQUARED)
        {
            final Font fontrenderer = Minecraft.getInstance().font;

            poseStack.pushPose();
            poseStack.translate(renderPos.getX() + 0.5d, renderPos.getY() + 0.6d, renderPos.getZ() + 0.5d);
            poseStack.mulPose(erm.cameraOrientation());
            poseStack.scale(0.014f, -0.014f, 0.014f);

            final float backgroundTextOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
            final int alphaMask = (int) (backgroundTextOpacity * 255.0F) << 24;

            final Matrix4f rawPosMatrix = poseStack.last().pose();

            for (int i = 0; i < cap; i += mergeEveryXListElements)
            {
                final MutableComponent renderText = Component.literal(
                    mergeEveryXListElements == 1 ? text.get(i) : text.subList(i, Math.min(i + mergeEveryXListElements, cap)).toString());
                final float textCenterShift = (float) (-fontrenderer.width(renderText) / 2);

                fontrenderer.drawInBatch(renderText,
                    textCenterShift,
                    0,
                    forceWhite ? 0xffffffff : 0x20ffffff,
                    false,
                    rawPosMatrix,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    alphaMask,
                    0x00f000f0);
                if (!forceWhite)
                {
                    fontrenderer.drawInBatch(renderText, textCenterShift, 0, 0xffffffff, false, rawPosMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, 0x00f000f0);
                }
                poseStack.translate(0.0d, fontrenderer.lineHeight + 1, 0.0d);
            }

            poseStack.popPose();
        }
    }

    public static final class RenderTypes extends RenderType
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

        private static final RenderType GLINT_LINES = create("structurize_glint_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.DEBUG_LINES,
            1 << 12,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setDepthTestState(NeverDepthTestStateShard.NEVER_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));

        private static final RenderType GLINT_LINES_WITH_WIDTH = create("structurize_glint_lines_with_width",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            1 << 13,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(GLINT_TRANSPARENCY)
                .setDepthTestState(AlwaysDepthTestStateShard.ALWAYS_DEPTH_TEST)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private static final RenderType LINES = create("structurize_lines",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.DEBUG_LINES,
            1 << 14,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));

        private static final RenderType LINES_WITH_WIDTH = create("structurize_lines_with_width",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            1 << 13,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private static final RenderType LINES_WITH_WIDTH_DEPTH_INVERT = create("structurize_lines_with_width_depth_invert",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            1 << 12,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(GREATER_DEPTH_TEST)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));

        private static final RenderType COLORED_TRIANGLES = create("structurize_colored_triangles",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            1 << 13,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private static final RenderType COLORED_TRIANGLES_NC_ND = create("structurize_colored_triangles_nc_nd",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            1 << 12,
            false,
            false,
            RenderType.CompositeState.builder()
                .setTextureState(NO_TEXTURE)
                .setShaderState(POSITION_COLOR_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(NeverDepthTestStateShard.NEVER_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setLayeringState(NO_LAYERING)
                .setOutputState(MAIN_TARGET)
                .setTexturingState(DEFAULT_TEXTURING)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));

        /**
         * Register our buffers
         */
        public static void registerBuffer(final RegisterRenderBuffersEvent event)
        {
            event.registerRenderBuffer(LINES);
            event.registerRenderBuffer(LINES_WITH_WIDTH);
            event.registerRenderBuffer(LINES_WITH_WIDTH_DEPTH_INVERT);
            event.registerRenderBuffer(GLINT_LINES);
            event.registerRenderBuffer(GLINT_LINES_WITH_WIDTH);
            event.registerRenderBuffer(COLORED_TRIANGLES);
            event.registerRenderBuffer(COLORED_TRIANGLES_NC_ND);
        }
    
        /**
         * Managed by structurize, ends above buffers in context similar to {@link RenderType#LINES}
         */
        public static void finishBuffer(final RenderLevelStageEvent event)
        {
            final MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            final Stage stage = event.getStage();

            if (stage == Stage.AFTER_BLOCK_ENTITIES)
            {
                bufferSource.endBatch(LINES_WITH_WIDTH_DEPTH_INVERT);

                bufferSource.endBatch(COLORED_TRIANGLES);
                bufferSource.endBatch(COLORED_TRIANGLES_NC_ND);

                bufferSource.endBatch(LINES);
                bufferSource.endBatch(LINES_WITH_WIDTH);

                // fallthrough into levelRenderer master endBatch
                // bufferSource.endBatch(GLINT_LINES);
                // bufferSource.endBatch(GLINT_LINES_WITH_WIDTH);
            }
        }

        public static class NeverDepthTestStateShard extends DepthTestStateShard
        {
            public static final DepthTestStateShard NEVER_DEPTH_TEST = new NeverDepthTestStateShard();

            private NeverDepthTestStateShard()
            {
                super("true_never", -1);
                setupState = () -> {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL30C.GL_NEVER);
                };
            }
        }

        public static class AlwaysDepthTestStateShard extends DepthTestStateShard
        {
            public static final DepthTestStateShard ALWAYS_DEPTH_TEST = new AlwaysDepthTestStateShard();

            private AlwaysDepthTestStateShard()
            {
                super("true_always", -1);
                setupState = () -> {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(GL30C.GL_ALWAYS);
                };
            }
        }
    }
}
