package com.ldtteam.structurize.util;

import java.util.List;
import java.util.OptionalDouble;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;

public class RenderUtils
{
    private static final int MAX_DEBUG_TEXT_RENDER_DIST_SQUARED = 32 * 32 * 32;
    public static final RenderType LINES_GLINT = RenderTypes.LINES_GLINT;

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     */
    public static void renderWhiteOutlineBox(final BlockPos posA, final BlockPos posB, final MatrixStack ms, final IVertexBuilder buffer)
    {
        renderBox(posA, posB, 1, 1, 1, 1, 0, ms, buffer);
    }

    /**
     * Render a box around two positions
     *
     * @param posA    First position
     * @param posB    Second position
     * @param red     red colour float 0 - 1
     * @param green   green colour float 0 - 1
     * @param blue    blue colour float 0 - 1
     * @param alpha   opacity 0 - 1
     * @param boxGrow size grow in every direction
     */
    public static void renderBox(final BlockPos posA,
        final BlockPos posB,
        final float red,
        final float green,
        final float blue,
        final float alpha,
        final double boxGrow,
        final MatrixStack matrixStack,
        final IVertexBuilder buffer)
    {
        final double minX = Math.min(posA.getX(), posB.getX()) - boxGrow;
        final double minY = Math.min(posA.getY(), posB.getY()) - boxGrow;
        final double minZ = Math.min(posA.getZ(), posB.getZ()) - boxGrow;

        final double maxX = Math.max(posA.getX(), posB.getX()) + 1 + boxGrow;
        final double maxY = Math.max(posA.getY(), posB.getY()) + 1 + boxGrow;
        final double maxZ = Math.max(posA.getZ(), posB.getZ()) + 1 + boxGrow;

        final Vector3d viewPosition = Minecraft.getInstance().getRenderManager().info.getProjectedView();

        matrixStack.push();
        matrixStack.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);

        WorldRenderer.drawBoundingBox(matrixStack, buffer, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);

        matrixStack.pop();
    }

    /**
     * Renders the given list of strings, 3 elements a row.
     *
     * @param pos         position to render at
     * @param text        text list
     * @param matrixStack stack to use
     */
    public static void renderDebugText(final BlockPos pos,
        final List<String> text,
        final MatrixStack matrixStack,
        final IRenderTypeBuffer buffer)
    {
        final EntityRendererManager erm = Minecraft.getInstance().getRenderManager();
        if (erm.getDistanceToCamera(pos.getX(), pos.getY(), pos.getZ()) <= MAX_DEBUG_TEXT_RENDER_DIST_SQUARED)
        {
            final Vector3d viewPosition = erm.info.getProjectedView();
            final FontRenderer fontrenderer = erm.getFontRenderer();

            matrixStack.push();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

            matrixStack.translate(pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5);
            matrixStack.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);
            matrixStack.rotate(erm.getCameraOrientation());
            matrixStack.scale(-0.025F, -0.025F, 0.025F);

            final float backgroundTextOpacity = Minecraft.getInstance().gameSettings.getTextBackgroundOpacity(0.25F);
            final int alphaMask = (int) (backgroundTextOpacity * 255.0F) << 24;

            final Matrix4f rawPosMatrix = matrixStack.getLast().getMatrix();

            final int cap = text.size();
            for (int i = 0; i < cap; i += 3)
            {
                final StringTextComponent renderText = new StringTextComponent(text.subList(i, Math.min(i + 2, cap)).toString());
                final float textCenterShift = (float) (-fontrenderer.func_238414_a_(renderText) / 2);

                fontrenderer
                    .func_238416_a_(renderText, textCenterShift, 0, 0x20ffffff, false, rawPosMatrix, buffer, true, alphaMask, 15728880);
                fontrenderer.func_238416_a_(renderText, textCenterShift, 0, 0xffffffff, false, rawPosMatrix, buffer, false, 0, 15728880);
                matrixStack.translate(0, 10, 0);
            }

            RenderSystem.disableBlend();
            matrixStack.pop();
        }
    }

    public static final class RenderTypes extends RenderType
    {
        public RenderTypes(final String nameIn,
            final VertexFormat formatIn,
            final int drawModeIn,
            final int bufferSizeIn,
            final boolean useDelegateIn,
            final boolean needsSortingIn,
            final Runnable setupTaskIn,
            final Runnable clearTaskIn)
        {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
            throw new IllegalStateException();
        }

        private static final RenderType LINES_GLINT = makeType("structurize_lines_glint",
            DefaultVertexFormats.POSITION_COLOR,
            1,
            256,
            RenderType.State.getBuilder()
                .line(new RenderState.LineState(OptionalDouble.empty()))
                .layer(field_239235_M_)
                .transparency(GLINT_TRANSPARENCY)
                .target(field_241712_U_)
                .writeMask(COLOR_WRITE)
                .cull(CULL_DISABLED)
                .depthTest(DEPTH_ALWAYS)
                .fog(NO_FOG)
                .build(false));
    }
}
