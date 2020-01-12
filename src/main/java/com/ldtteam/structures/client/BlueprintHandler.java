package com.ldtteam.structures.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Log;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The Blueprint render handler on the client side.
 */
public final class BlueprintHandler
{
    /**
     * A static instance on the client.
     */
    private static final BlueprintHandler ourInstance = new BlueprintHandler();

    /**
     * The builder cache.
     */
    private final Cache<Blueprint, BlueprintRenderer> blueprintBufferBuilderCache =
      CacheBuilder.newBuilder()
        .maximumSize(50)
        .removalListener((RemovalListener<Blueprint, BlueprintRenderer>) notification -> notification.getValue().close())
        .build();

    /**
     * Cached entity renderer.
     */
    //private RenderManager entityRenderer;

    /**
     * Private constructor to hide public one.
     */
    private BlueprintHandler()
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
    public static BlueprintHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Draw a blueprint with a rotation, mirror and offset.
     *
     * @param blueprint      the wayPointBlueprint to draw.
     * @param rotation      its rotation.
     * @param mirror        its mirror.
     * @param drawingOffset its offset.
     */
    public void draw(final Blueprint blueprint, final Rotation rotation, final Mirror mirror, final Vec3d drawingOffset, final MatrixStack stack, final float partialTicks)
    {
        if (blueprint == null)
        {
            Log.getLogger().warn("Trying to draw null blueprint!");
            return;
        }

        try
        {
            blueprintBufferBuilderCache.get(blueprint, () -> BlueprintRenderer.buildRendererForBlueprint(blueprint)).draw(rotation, mirror, drawingOffset, stack, partialTicks);
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
     * @param blueprint the blueprint.
     */
    public void drawBlueprintAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Blueprint blueprint, final MatrixStack stack)
    {
        if (points.isEmpty())
        {
            return;
        }

        final Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        for (final BlockPos coord : points)
        {
            final Vec3d vec = new Vec3d(coord.down()).subtract(projectedView);
            draw(blueprint, Rotation.NONE, Mirror.NONE, vec, stack, partialTicks);
        }
    }
}
