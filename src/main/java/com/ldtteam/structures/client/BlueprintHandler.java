package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Log;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import java.lang.ref.SoftReference;
import java.util.List;

/**
 * The Blueprint render handler on the client side.
 */
public final class BlueprintHandler
{
    /**
     * A static instance on the client.
     */
    private static final BlueprintHandler ourInstance = new BlueprintHandler();
    private static final int CACHE_SIZE = 30;
    private static final long CACHE_EVICT_TIME = 45_000L;

    private final Int2ObjectArrayMap<SoftReference<BlueprintRenderer>> rendererCache = new Int2ObjectArrayMap<>(CACHE_SIZE);
    private final Int2LongArrayMap evictTimeCache = new Int2LongArrayMap(CACHE_SIZE);

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
     * @param blueprint the wayPointBlueprint to draw.
     * @param pos       its position.
     */
    public void draw(final Blueprint blueprint, final BlockPos pos, final MatrixStack stack, final float partialTicks)
    {
        if (blueprint == null)
        {
            Log.getLogger().warn("Trying to draw null blueprint!");
            return;
        }
        Minecraft.getInstance().getProfiler().startSection("struct_render_cache");

        final int blueprintHash = blueprint.hashCode();
        final SoftReference<BlueprintRenderer> rendererRef = rendererCache.get(blueprintHash);
        final BlueprintRenderer renderer;

        if (rendererRef == null || rendererRef.get() == null)
        {
            renderer = BlueprintRenderer.buildRendererForBlueprint(blueprint);
            rendererCache.put(blueprintHash, new SoftReference<>(renderer));
        }
        else
        {
            renderer = rendererRef.get();
        }

        evictTimeCache.put(blueprintHash, System.currentTimeMillis());
        renderer.updateBlueprint(blueprint);
        renderer.draw(pos, stack, partialTicks);

        Minecraft.getInstance().getProfiler().endSection();
    }

    /**
     * Cleans entries that are older than CACHE_EVICT_TIME.
     */
    public void cleanCache()
    {
        for (final Int2LongMap.Entry entry : evictTimeCache.int2LongEntrySet())
        {
            if (entry.getLongValue() + CACHE_EVICT_TIME < System.currentTimeMillis())
            {
                final int removeHash = entry.getIntKey();
                final SoftReference<BlueprintRenderer> removeRendererRef = rendererCache.remove(removeHash);
                evictTimeCache.remove(removeHash);
                if (removeRendererRef != null && removeRendererRef.get() != null)
                {
                    removeRendererRef.get().close();
                }
            }
        }
    }

    /**
     * Render a blueprint at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     * @param blueprint    the blueprint.
     */
    public void drawBlueprintAtListOfPositions(final List<BlockPos> points, final float partialTicks, final Blueprint blueprint, final MatrixStack stack)
    {
        if (points.isEmpty())
        {
            return;
        }

        for (final BlockPos coord : points)
        {
            draw(blueprint, coord.down(), stack, partialTicks);
        }
    }
}
