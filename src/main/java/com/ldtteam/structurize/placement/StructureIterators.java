package com.ldtteam.structurize.placement;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.placement.structure.IStructureHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class to store and handle structure iterators.
 */
public class StructureIterators
{
    /**
     * The list of producers.
     */
    private static final Map<String, Function<IStructureHandler, AbstractBlueprintIterator>> iteratorProducers = new HashMap<>();

    /*
     * Pre-existing iterators.
     */
    static
    {
        iteratorProducers.put("default", BlueprintIteratorDefault::new);
        iteratorProducers.put("inwardcircle", BlueprintIteratorInwardCircle::new);
        iteratorProducers.put("inwardcircleheight1", handler -> new BlueprintIteratorInwardCircleHeight(handler, 1));
        iteratorProducers.put("inwardcircleheight2", handler -> new BlueprintIteratorInwardCircleHeight(handler, 2));
        iteratorProducers.put("inwardcircleheight3", handler -> new BlueprintIteratorInwardCircleHeight(handler, 3));
        iteratorProducers.put("inwardcircleheight4", handler -> new BlueprintIteratorInwardCircleHeight(handler, 4));
        iteratorProducers.put("hilbert", BlueprintIteratorHilbert::new);
        iteratorProducers.put("random", BlueprintIteratorRandom::new);
    }
    /**
     * Register a new producer.
     *
     * @param id       the id of the producer.
     * @param producer the producer.
     */
    public static void registerIterator(final String id, final Function<IStructureHandler, AbstractBlueprintIterator> producer)
    {
        iteratorProducers.put(id, producer);
    }

    /**
     * Get a list of all the possible settings.
     * @return the list of settings.
     */
    public static List<String> getKeySet()
    {
        return ImmutableList.copyOf(iteratorProducers.keySet());
    }

    /**
     * Get an iterator from id and with the applied structure handler.
     *
     * @param id      the unique id.
     * @param handler the handler.
     * @return the instance of the iterator.
     */
    public static AbstractBlueprintIterator getIterator(final String id, final IStructureHandler handler)
    {
        final Function<IStructureHandler, AbstractBlueprintIterator> iterator = iteratorProducers.get(id);
        if (iterator == null)
        {
            Log.getLogger().warn("Could not find iterator for value:" + id + " using default instead!");
            return new BlueprintIteratorDefault(handler);
        }

        return iterator.apply(handler);
    }
}
