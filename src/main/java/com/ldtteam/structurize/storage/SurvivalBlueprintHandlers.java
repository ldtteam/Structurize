package com.ldtteam.structurize.storage;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to register a survival blueprint handler.
 */
public class SurvivalBlueprintHandlers
{
    /**
     * Map of handlers.
     */
    private static final Map<String, ISurvivalBlueprintHandler> handlers = new HashMap<>();

    /**
     * Our immutable list of survival handlers, internal use only.
     */
    private static ImmutableList<ISurvivalBlueprintHandler> survivalHandlers = ImmutableList.of();

    /**
     * Get a specific handler by id.
     * @param id the id of the handler.
     * @return the handler instance.
     */
    public static ISurvivalBlueprintHandler getHandler(final String id)
    {
        return handlers.get(id);
    }

    /**
     * Getthe full list of handlers.
     * @return a list.
     */
    public static List<ISurvivalBlueprintHandler> getHandlers()
    {
        return survivalHandlers;
    }

    /**
     * Register a new survival blueprint handler.
     * @param handler the handler itself.
     */
    public static void registerHandler(final ISurvivalBlueprintHandler handler)
    {
        handlers.put(handler.getId(), handler);
        survivalHandlers = ImmutableList.copyOf(handlers.values());
    }

    /**
     * Get all handlers that can take over the placement operation.
     * @return
     */
    public static List<ISurvivalBlueprintHandler> getMatchingHandlers(final Blueprint blueprint, final ClientLevel level, final Player player, final BlockPos pos, final RotationMirror rotMir)
    {
        final List<ISurvivalBlueprintHandler> matchingHandlers = new ArrayList<>();
        for (final ISurvivalBlueprintHandler handler : handlers.values())
        {
            if (handler.canHandle(blueprint, level, player, pos, rotMir))
            {
                matchingHandlers.add(handler);
            }
        }
        return matchingHandlers;
    }
}
