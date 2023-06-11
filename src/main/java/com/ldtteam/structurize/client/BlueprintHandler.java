package com.ldtteam.structurize.client;

import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RenderLevelStageEvent;

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
    private static final BlueprintHandler ourInstance = new BlueprintHandler();
    private static final int CACHE_SIZE = 30;
    private static final long CACHE_EVICT_TIME = 45_000L;

    private final Int2ObjectArrayMap<BlueprintRenderer> rendererCache = new Int2ObjectArrayMap<>(CACHE_SIZE);
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
     * @param previewData the blueprint and context to draw.
     * @param pos       its position.
     */
    public void draw(final BlueprintPreviewData previewData, final BlockPos pos, final RenderLevelStageEvent ctx)
    {
        if (previewData == null || previewData.getBlueprint() == null)
        {
            Log.getLogger().warn("Trying to draw null blueprint!");
            return;
        }
        Minecraft.getInstance().getProfiler().push("struct_render_cache");

        final int blueprintHash = previewData.getBlueprint().hashCode();
        final BlueprintRenderer rendererRef = rendererCache.get(blueprintHash);
        final BlueprintRenderer renderer = rendererRef == null ? BlueprintRenderer.buildRendererForBlueprint(previewData.getBlueprint()) : rendererRef;

        if (rendererRef == null)
        {
            rendererCache.put(blueprintHash, renderer);
        }

        renderer.updateBlueprint(previewData);
        renderer.draw(previewData, pos, ctx);
        evictTimeCache.put(blueprintHash, System.currentTimeMillis());

        Minecraft.getInstance().getProfiler().pop();
    }

    /**
     * Cleans entries that are older than CACHE_EVICT_TIME.
     */
    public void cleanCache()
    {
        final long now = System.currentTimeMillis();
        final Iterator<Int2LongMap.Entry> iter = evictTimeCache.int2LongEntrySet().iterator();

        while (iter.hasNext())
        {
            final Int2LongMap.Entry entry = iter.next();
            if (entry.getLongValue() + CACHE_EVICT_TIME < now)
            {
                rendererCache.remove(entry.getIntKey()).close();
                iter.remove();
            }
        }
    }

    /**
     * Clear all entries.
     */
    public void clearCache()
    {
        evictTimeCache.clear();
        rendererCache.values().forEach(BlueprintRenderer::close);
        rendererCache.clear();
    }

    /**
     * Render a blueprint at a list of points.
     *
     * @param points       the points to render it at.
     * @param partialTicks the partial ticks.
     */
    public void drawAtListOfPositions(final BlueprintPreviewData previewData,
        final List<BlockPos> points,
        final RenderLevelStageEvent ctx)
    {
        if (points.isEmpty() || previewData == null || previewData.getBlueprint() == null)
        {
            return;
        }

        Minecraft.getInstance().getProfiler().push("struct_render_multi");

        final int blueprintHash = previewData.getBlueprint().hashCode();
        final BlueprintRenderer rendererRef = rendererCache.get(blueprintHash);
        final BlueprintRenderer renderer = rendererRef == null ? BlueprintRenderer.buildRendererForBlueprint(previewData.getBlueprint()) : rendererRef;

        if (rendererRef == null)
        {
            rendererCache.put(blueprintHash, renderer);
        }

        renderer.updateBlueprint(previewData);

        for (final BlockPos coord : points)
        {
            renderer.draw(previewData, coord, ctx);
        }

        evictTimeCache.put(blueprintHash, System.currentTimeMillis());

        Minecraft.getInstance().getProfiler().pop();
    }
}
