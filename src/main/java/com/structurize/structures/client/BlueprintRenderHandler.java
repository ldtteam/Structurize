package com.structurize.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.structurize.api.util.Log;
import com.structurize.structures.blueprints.v1.Blueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The wayPointblueprint render handler on the client side.
 */
public final class BlueprintRenderHandler
{
    /**
     * A static instance on the client.
     */
    private static final BlueprintRenderHandler ourInstance = new BlueprintRenderHandler();

    /**
     * The builder cache.
     */
    private final Cache<Blueprint, BlueprintRenderer> blueprintBufferBuilderCache =
      CacheBuilder.newBuilder()
        .maximumSize(50)
        .removalListener((RemovalListener<Blueprint, BlueprintRenderer>) notification -> notification.getValue().getTessellator().getBuffer().deleteGlBuffers())
        .build();

    /**
     * Cached entity renderer.
     */
    private RenderManager entityRenderer;

    /**
     * Private constructor to hide public one.
     */
    private BlueprintRenderHandler()
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
    public static BlueprintRenderHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Draw a wayPointBlueprint with a rotation, mirror and offset.
     *
     * @param blueprint     the wayPointBlueprint to draw.
     * @param rotation      its rotation.
     * @param mirror        its mirror.
     * @param drawingOffset its offset.
     */
    public void draw(final Blueprint blueprint, final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset)
    {
        if (blueprint == null)
        {
            Log.getLogger().warn("Trying to draw null blueprint!");
            return;
        }

        try
        {
            blueprintBufferBuilderCache.get(blueprint, () -> BlueprintRenderer.buildRendererForBlueprint(blueprint)).draw(rotation, mirror, drawingOffset);
        }
        catch (ExecutionException e)
        {
            Log.getLogger().error(e);
        }
    }

    /**
     * Render a blueprint at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     * @param blueprint    the blueprint.
     */
    public void drawBlueprintAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Blueprint blueprint)
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

            draw(blueprint, Rotation.NONE, Mirror.NONE, renderOffset);
        }
    }
}
