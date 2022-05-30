package com.ldtteam.blockout;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
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
        drawOutlineRect(ms.last()
            .pose(), x, y, w, h, (color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff, (color >> 24) & 0xff, lineWidth);
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

        final BufferBuilder vertexBuffer = Tessellator.getInstance().getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        GL11.glLineWidth(lineWidth);
        vertexBuffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        vertexBuffer.vertex(matrix, x, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x + w, y, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x + w, y + h, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.vertex(matrix, x, y + h, 0.0f).color(red, green, blue, alpha).endVertex();
        vertexBuffer.end();
        WorldVertexBufferUploader.end(vertexBuffer);

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /**
     * Render an entity on a GUI.
     * @param matrixStack matrix
     * @param x horizontal center position
     * @param y vertical bottom position
     * @param scale scaling factor
     * @param headYaw adjusts look rotation
     * @param yaw adjusts body rotation
     * @param pitch adjusts look rotation
     * @param entity the entity to render
     */
    public static void drawEntity(final MatrixStack matrixStack, final int x, final int y, final double scale,
                                  final float headYaw, final float yaw, final float pitch, final Entity entity)
    {
        final LivingEntity livingEntity = (entity instanceof LivingEntity) ? (LivingEntity) entity : null;
        final Minecraft mc = Minecraft.getInstance();
        if (entity.level == null) entity.level = mc.level;
        matrixStack.pushPose();
        matrixStack.translate((float) x, (float) y, 1050.0F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale((float) scale, (float) scale, (float) scale);
        final Quaternion pitchRotation = Vector3f.XP.rotationDegrees(pitch);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        matrixStack.mulPose(pitchRotation);
        final float oldYawOffset = livingEntity == null ? 0F : livingEntity.yBodyRot;
        final float oldPrevYawHead = livingEntity == null ? 0F : livingEntity.yHeadRotO;
        final float oldYawHead = livingEntity == null ? 0F : livingEntity.yHeadRot;
        final float oldYaw = entity.yRot;
        final float oldPitch = entity.xRot;
        entity.yRot = 180.0F + (float) headYaw;
        entity.xRot = -pitch;
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = 180.0F + yaw;
            livingEntity.yHeadRot = entity.yRot;
            livingEntity.yHeadRotO = entity.yRot;
        }
        final EntityRendererManager entityrenderermanager = mc.getEntityRenderDispatcher();
        pitchRotation.conj();
        entityrenderermanager.overrideCameraOrientation(pitchRotation);
        entityrenderermanager.setRenderShadow(false);
        final IRenderTypeBuffer.Impl buffers = mc.renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> entityrenderermanager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, buffers, 0x00F000F0));
        buffers.endBatch();
        entityrenderermanager.setRenderShadow(true);
        entity.yRot = oldYaw;
        entity.xRot = oldPitch;
        if (livingEntity != null)
        {
            livingEntity.yBodyRot = oldYawOffset;
            livingEntity.yHeadRotO = oldPrevYawHead;
            livingEntity.yHeadRot = oldYawHead;
        }
        matrixStack.popPose();
    }
}
