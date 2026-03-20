package com.ldtteam.structurize.client.rendertask;

import com.ldtteam.structurize.client.rendercontext.WorldEventRenderContext;
import com.ldtteam.structurize.client.rendertask.task.IRenderTask;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenderTaskManager
{
    // Uses:
    // Render a box with: Text, color, duration, transparency, infront/behind blocks
    // Render text at a position(box optional?)
    // Render a large box in world(claim borders, lumberjack etc)
    // Timed task: enables/disables citizen glowing effect with duration

    /**
     * A position to highlight with a group and key.
     */
    private static final Map<String, Map<String, IRenderTask>> RENDER_TASKS = new LinkedHashMap<>();

    /**
     * Highlights positions
     *
     * @param context rendering context
     */
    public static void render(final WorldEventRenderContext context)
    {
        if (RENDER_TASKS.isEmpty())
        {
            return;
        }

        for (final Iterator<Map<String, IRenderTask>> groups = RENDER_TASKS.values().iterator(); groups.hasNext(); )
        {
            final Map<String, IRenderTask> group = groups.next();
            for (final Iterator<IRenderTask> renderTaskIterator = group.values().iterator(); renderTaskIterator.hasNext(); )
            {
                final IRenderTask renderTask = renderTaskIterator.next();

                if (renderTask.shouldRenderIn(context.stageEvent.getStage()))
                {
                    renderTask.render(context);
                }
            }
        }
    }

    /**
     * Client tick callback
     */
    public static void onClientTick()
    {
        for (final Iterator<Map<String, IRenderTask>> groups = RENDER_TASKS.values().iterator(); groups.hasNext(); )
        {
            final Map<String, IRenderTask> group = groups.next();
            for (final Iterator<IRenderTask> containers = group.values().iterator(); containers.hasNext(); )
            {
                final IRenderTask renderDataContainer = containers.next();
                boolean isDone = renderDataContainer.tick();
                if (isDone)
                {
                    containers.remove();
                }
            }

            if (group.isEmpty())
            {
                groups.remove();
            }
        }
    }

    /**
     * Clears all highlight items for the given group key.
     *
     * @param key the key to remove the render data for.
     */
    public static void clearHighlightsForKey(final String key)
    {
        RENDER_TASKS.remove(key);
    }

    /**
     * Adds a highlight item for the given key.
     *
     * @param key the group key of the item to render.
     * @return the previous entry or null
     */
    public static IRenderTask addRenderTask(final String key, final IRenderTask task)
    {
        return RENDER_TASKS.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(task.id(), task);
    }

    @Nullable
    public static Map<String, IRenderTask> getTasksByGroup(final String groupID)
    {
        return RENDER_TASKS.get(groupID);
    }

    public static boolean removeTaskGroup(final String groupID)
    {
        return RENDER_TASKS.remove(groupID) != null;
    }

    public static boolean removeTaskEntry(final String groupID, final String taskID)
    {
        Map<String, IRenderTask> entry = RENDER_TASKS.get(groupID);
        if (entry != null)
        {
            return entry.remove(taskID) != null;
        }

        return false;
    }
}
