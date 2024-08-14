package com.ldtteam.structurize.storage.rendering;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToClient;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;

import java.util.*;
import java.util.Map.Entry;

/**
 * Rendering cache for boxes, blueprints, etc.
 */
public class RenderingCache
{
    /**
     * Boxes to render.
     */
    private static Map<String, BoxPreviewData> boxRenderingCache = new HashMap<>();

    /**
     * Blueprints to render.
     */
    private static Map<String, BlueprintPreviewData> blueprintRenderingCache = new HashMap<>();

    /**
     * Check if there is a blueprint with the id in the cache.
     * @param key the key to check for.
     * @return true if so.
     */
    public static boolean hasBlueprint(final String key)
    {
        return blueprintRenderingCache.containsKey(key);
    }

    /**
     * Get the preview data for a box.
     * @param key the key of the box.
     * @return the preview data.
     */
    public static BoxPreviewData getBoxPreviewData(final String key)
    {
        return boxRenderingCache.get(key);
    }

    /**
     * Get the preview data for a blueprint.
     * @param key the key of the blueprint.
     * @return the preview data.
     */
    public static BlueprintPreviewData getBlueprintPreviewData(final String key)
    {
        return blueprintRenderingCache.get(key);
    }
    /**
     * Get a list of all blueprints to render.
     * @return the preview data.
     */
    public static Collection<BlueprintPreviewData> getBlueprintsToRender()
    {
        return blueprintRenderingCache.values();
    }

    /**
     * Get a list of all blueprints to render.
     * @return the preview data.
     */
    public static Collection<BoxPreviewData> getBoxesToRender()
    {
        return boxRenderingCache.values();
    }

    /**
     * Queue a box to be rendered.
     * @param key the key to queue it under.
     * @param boxPreviewData the preview data.
     */
    public static void queue(final String key, final BoxPreviewData boxPreviewData)
    {
        boxRenderingCache.put(key, boxPreviewData);
    }

    /**
     * Queue a blueprint to be rendered.
     * @param key the key to queue it under.
     * @param boxPreviewData the preview data.
     */
    public static void queue(final String key, final BlueprintPreviewData boxPreviewData)
    {
        blueprintRenderingCache.put(key, boxPreviewData);
    }

    /**
     * Remove an item from the cache.
     * @param key the key of the item to be removed.
     * @return the removed data.
     */
    public static BoxPreviewData removeBox(final String key)
    {
        return boxRenderingCache.remove(key);
    }

    /**
     * Remove an item from the cache.
     * @param key the key of the item to be removed.
     * @return the removed data.
     */
    public static BlueprintPreviewData removeBlueprint(final String key)
    {
        return blueprintRenderingCache.remove(key);
    }

    /**
     * Get or create a blueprint preview data cache object.
     * @param key the key to look for.
     * @return the blueprint preview data with the key or a new one.
     */
    public static BlueprintPreviewData getOrCreateBlueprintPreviewData(final String key)
    {
        return blueprintRenderingCache.computeIfAbsent(key, k -> new BlueprintPreviewData());
    }

    /**
     * @return true when should use light level from {@link #getOurLightLevel()}
     */
    @Deprecated
    public static boolean forceLightLevel()
    {
        return Structurize.getConfig().getClient().rendererLightLevel.get() >= 0;
    }

    /**
     * @return static light level
     */
    @Deprecated
    public static int getOurLightLevel()
    {
        return Structurize.getConfig().getClient().rendererLightLevel.get();
    }

    /**
     * Clean the rendering caches.
     */
    public static void clear()
    {
        blueprintRenderingCache.clear();
        boxRenderingCache.clear();
    }

    /**
     * Removes all shared previews
     */
    public static void removeSharedPreviews()
    {
        blueprintRenderingCache.keySet().removeIf(key -> key.startsWith(SyncPreviewCacheToClient.SHARED_PREFIX));
    }
}
