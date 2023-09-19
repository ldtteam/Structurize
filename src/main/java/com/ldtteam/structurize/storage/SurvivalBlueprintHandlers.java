package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
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
     * List of lists of mutually exclusive handlers.
     */
    private static final Map<String, List<String>> mutuallyExclusiveHandlers = new HashMap<>();

    /**
     * Get a specific handler by id.
     *
     * @param id the id of the handler.
     * @return the handler instance.
     */
    public static ISurvivalBlueprintHandler getHandler(final String id)
    {
        return handlers.get(id);
    }

    /**
     * Check if there are no registered handlers.
     *
     * @return true if no handlers are present.
     */
    public static boolean noHandlers()
    {
        return handlers.isEmpty();
    }

    /**
     * Register a new survival blueprint handler.
     *
     * @param handler the handler itself.
     */
    public static void registerHandler(final ISurvivalBlueprintHandler handler)
    {
        handlers.put(handler.getId(), handler);
    }

    /**
     * Register a new survival blueprint handler.
     * These handlers are "mutually exclusive", meaning that during a call for placement,
     * only one of these (whichever is hit first) is used for the placement.
     *
     * @param blueprintHandlers the blueprint handlers.
     */
    public static void registerMutuallyExclusiveHandler(final ISurvivalBlueprintHandler... blueprintHandlers)
    {
        List<String> handlerIds = new ArrayList<>();

        for (final ISurvivalBlueprintHandler blueprintHandler : blueprintHandlers)
        {
            handlers.put(blueprintHandler.getId(), blueprintHandler);
            mutuallyExclusiveHandlers.put(blueprintHandler.getId(), new ArrayList<>());
            handlerIds.add(blueprintHandler.getId());
        }

        for (final String handlerId : handlerIds)
        {
            for (final String otherHandlerId : handlerIds)
            {
                if (!handlerId.equals(otherHandlerId))
                {
                    mutuallyExclusiveHandlers.get(handlerId).add(otherHandlerId);
                }
            }
        }
    }

    /**
     * Get all handlers that can take over the placement operation.
     *
     * @return the list of matching handlers.
     */
    public static List<ISurvivalBlueprintHandler> getMatchingHandlers(
      final Blueprint blueprint,
      final ClientLevel level,
      final Player player,
      final BlockPos pos,
      final PlacementSettings settings)
    {
        final List<String> matchingHandlerIds = new ArrayList<>();
        final List<ISurvivalBlueprintHandler> matchingHandlers = new ArrayList<>();

        for (final ISurvivalBlueprintHandler handler : handlers.values())
        {
            boolean shouldSkip = false;
            final List<String> mutuallyExclusives = mutuallyExclusiveHandlers.get(handler.getId());
            if (mutuallyExclusives != null)
            {
                for (final String otherHandlerId : mutuallyExclusives)
                {
                    if (matchingHandlerIds.contains(otherHandlerId))
                    {
                        shouldSkip = true;
                        break;
                    }
                }
            }

            if (shouldSkip)
            {
                continue;
            }

            if (handler.canHandle(blueprint, level, player, pos, settings))
            {
                matchingHandlerIds.add(handler.getId());
                matchingHandlers.add(handler);
            }
        }
        return matchingHandlers;
    }
}
