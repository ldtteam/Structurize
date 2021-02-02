package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Log;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
import java.util.List;

/**
 * The Blueprint render handler on the client side.
 */
public final class BlueprintHandler
{
    /**
     * A static instance on the client.
     */
    private static final BlueprintHandler ourInstance      = new BlueprintHandler();
    private static final int              CACHE_SIZE       = 30;
    private static final long             CACHE_EVICT_TIME = 45_000L;

    private final Int2ObjectArrayMap<BlueprintRenderer> dynamicRendererCache = new Int2ObjectArrayMap<>(CACHE_SIZE);
    private final Int2ObjectArrayMap<BlueprintRenderer> staticRendererCache  = new Int2ObjectArrayMap<>(CACHE_SIZE);

    private final Int2LongArrayMap dynamicEvictTimeCache = new Int2LongArrayMap(CACHE_SIZE);
    private final Int2LongArrayMap staticEvictTimeCache  = new Int2LongArrayMap(CACHE_SIZE);

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
    @Deprecated // INTERNAL USE ONLY
    public static BlueprintHandler getInstance()
    {
        return ourInstance;
    }

    /**
     * Draw a blueprint with a rotation, mirror and offset.
     *
     * @param blueprint    the wayPointBlueprint to draw.
     * @param pos          its position.
     * @param partialTicks partial ticks
     * @param stack        the matrix stack.
     * @param dynamic      if a dynamic (changes rotation and mirror) or static blueprint.
     */
    public void draw(final Blueprint blueprint, final BlockPos pos, final MatrixStack stack, final float partialTicks, final boolean dynamic)
    {
        if (blueprint == null)
        {
            Log.getLogger().warn("Trying to draw null blueprint!");
            return;
        }
        Minecraft.getInstance().getProfiler().startSection("struct_render_cache");

        final int blueprintHash = blueprint.hashCode();

        final BlueprintRenderer rendererRef = dynamic ? dynamicRendererCache.get(blueprintHash) : staticRendererCache.get(blueprintHash);
        final BlueprintRenderer renderer = rendererRef == null ? BlueprintRenderer.buildRendererForBlueprint(blueprint) : rendererRef;

        if (rendererRef == null)
        {
            if (dynamic)
            {
                dynamicRendererCache.put(blueprintHash, renderer);
            }
            else
            {
                staticRendererCache.put(blueprintHash, renderer);
            }
        }

        if (dynamic)
        {
            renderer.updateBlueprint(blueprint);
        }

        renderer.draw(pos, stack, partialTicks);
        if (dynamic)
        {
            dynamicEvictTimeCache.put(blueprintHash, System.currentTimeMillis());
        }
        else
        {
            staticEvictTimeCache.put(blueprintHash, System.currentTimeMillis());
        }

        Minecraft.getInstance().getProfiler().endSection();
    }

    /**
     * Cleans entries that are older than CACHE_EVICT_TIME.
     */
    public void cleanCache()
    {
        long now = System.currentTimeMillis();
        final Iterator<Int2LongMap.Entry> dynIter = dynamicEvictTimeCache.int2LongEntrySet().iterator();

        while (dynIter.hasNext())
        {
            final Int2LongMap.Entry entry = dynIter.next();
            if (entry.getLongValue() + CACHE_EVICT_TIME < now)
            {
                dynamicRendererCache.remove(entry.getIntKey()).close();
                dynIter.remove();
            }
        }

        now = System.currentTimeMillis();
        final Iterator<Int2LongMap.Entry> statIter = staticEvictTimeCache.int2LongEntrySet().iterator();

        while (statIter.hasNext())
        {
            final Int2LongMap.Entry entry = statIter.next();
            if (entry.getLongValue() + CACHE_EVICT_TIME < now)
            {
                staticRendererCache.remove(entry.getIntKey()).close();
                statIter.remove();
            }
        }
    }

    /**
     * Clear all entries.
     */
    public void clearCache()
    {
        dynamicEvictTimeCache.clear();
        dynamicRendererCache.values().forEach(BlueprintRenderer::close);
        dynamicRendererCache.clear();
        staticEvictTimeCache.clear();
        staticRendererCache.values().forEach(BlueprintRenderer::close);
        staticRendererCache.clear();
    }

    /**
     * Render a blueprint at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     * @param blueprint    the blueprint.
     * @param stack the matrix stack.
     * @param dynamic if dynamic (changes rotation and mirror) or static.
     */
    public void drawAtListOfPositions(
      final Blueprint blueprint,
      final List<BlockPos> points,
      final MatrixStack stack,
      final float partialTicks,
      final boolean dynamic)
    {
        if (points.isEmpty())
        {
            return;
        }

        Minecraft.getInstance().getProfiler().startSection("struct_render_multi");

        final int blueprintHash = blueprint.hashCode();
        final BlueprintRenderer rendererRef = dynamic ? dynamicRendererCache.get(blueprintHash) : staticRendererCache.get(blueprintHash);
        final BlueprintRenderer renderer = rendererRef == null ? BlueprintRenderer.buildRendererForBlueprint(blueprint) : rendererRef;

        if (rendererRef == null)
        {
            if (dynamic)
            {
                dynamicRendererCache.put(blueprintHash, renderer);
            }
            else
            {
                staticRendererCache.put(blueprintHash, renderer);
            }
        }

        if (dynamic)
        {
            renderer.updateBlueprint(blueprint);
        }

        for (final BlockPos coord : points)
        {
            renderer.draw(coord.down(), stack, partialTicks);
        }

        if (dynamic)
        {
            dynamicEvictTimeCache.put(blueprintHash, System.currentTimeMillis());
        }
        else
        {
            staticEvictTimeCache.put(blueprintHash, System.currentTimeMillis());
        }

        Minecraft.getInstance().getProfiler().endSection();
    }
}
