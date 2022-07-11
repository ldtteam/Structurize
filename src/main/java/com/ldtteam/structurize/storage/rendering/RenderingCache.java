package com.ldtteam.structurize.storage.rendering;

import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.types.ScanPreviewData;

import java.util.HashMap;
import java.util.Map;

/**
 * Rendering cache for boxes, blueprints, etc.
 */
public class RenderingCache
{
    /**
     * Boxes to render.
     */
    public static Map<String, ScanPreviewData> boxRenderingCache = new HashMap<>();

    /**
     * Blueprints to render.
     */
    public static Map<String, BlueprintPreviewData> blueprintRenderingCache = new HashMap<>();

    /**
     * Get or create a blueprint preview data cache object.
     * @param key the key to look for.
     * @return the blueprint preview data with the key or a new one.
     */
    public static BlueprintPreviewData getOrCreateBlueprintPreviewData(final String key)
    {
        final BlueprintPreviewData data = blueprintRenderingCache.getOrDefault(key, new BlueprintPreviewData());
        blueprintRenderingCache.put(key, data);
        return data;
    }

    /**
     * @return true when should use light level from {@link #getOurLightLevel()}
     */
    public static boolean forceLightLevel()
    {
        return true;
    }

    /**
     * @return static light level
     */
    public static int getOurLightLevel()
    {
        return 15;
    }
}
