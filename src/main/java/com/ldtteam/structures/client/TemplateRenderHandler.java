package com.ldtteam.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.structurize.api.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The wayPointTemplate render handler on the client side.
 */
public final class TemplateRenderHandler
{
    /**
     * A static instance on the client.
     */
    private static final TemplateRenderHandler ourInstance = new TemplateRenderHandler();

    /**
     * The builder cache.
     */
    private final Cache<Template, TemplateRenderer> templateBufferBuilderCache =
      CacheBuilder.newBuilder()
        .maximumSize(50)
        .removalListener((RemovalListener<Template, TemplateRenderer>) notification -> notification.getValue().getTessellator().getBuffer().deleteGlBuffers())
        .build();

    /**
     * Cached entity renderer.
     */
    //private RenderManager entityRenderer;

    /**
     * Private constructor to hide public one.
     */
    private TemplateRenderHandler()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get the static instance.
     *
     * @return a static instance of this class.
     */
    public static TemplateRenderHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Draw a wayPointTemplate with a rotation, mirror and offset.
     *
     * @param template      the wayPointTemplate to draw.
     * @param rotation      its rotation.
     * @param mirror        its mirror.
     * @param drawingOffset its offset.
     */
    public void draw(final Template template, final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset)
    {
        if (template == null)
        {
            Log.getLogger().warn("Trying to draw null template!");
            return;
        }

        try
        {
            templateBufferBuilderCache.get(template, () -> TemplateRenderer.buildRendererForTemplate(template)).draw(rotation, mirror, drawingOffset);
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error(e);
        }
    }

    /**
     * Render a template at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     * @param template the template.
     */
    public void drawTemplateAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Template template)
    {
        if (points.isEmpty())
        {
            return;
        }

        final EntityPlayer perspectiveEntity = Minecraft.getMinecraft().player;
        final double interpolatedEntityPosX = perspectiveEntity.lastTickPosX + (perspectiveEntity.posX - perspectiveEntity.lastTickPosX) * partialTicks;
        final double interpolatedEntityPosY = perspectiveEntity.lastTickPosY + (perspectiveEntity.posY - perspectiveEntity.lastTickPosY) * partialTicks;
        final double interpolatedEntityPosZ = perspectiveEntity.lastTickPosZ + (perspectiveEntity.posZ - perspectiveEntity.lastTickPosZ) * partialTicks;

        for (final BlockPos coord : points)
        {
            final BlockPos pos = coord.down();
            final double renderOffsetX = pos.getX() - interpolatedEntityPosX;
            final double renderOffsetY = pos.getY() - interpolatedEntityPosY;
            final double renderOffsetZ = pos.getZ() - interpolatedEntityPosZ;
            final Vector3d renderOffset = new Vector3d();
            renderOffset.x = renderOffsetX;
            renderOffset.y = renderOffsetY;
            renderOffset.z = renderOffsetZ;

            draw(template, Rotation.NONE, Mirror.NONE, renderOffset);
        }
    }
}
