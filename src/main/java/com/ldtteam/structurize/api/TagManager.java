package com.ldtteam.structurize.api;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.constants.Constants.GROUNDLEVEL_TAG;
import static com.ldtteam.structurize.api.constants.Constants.INVISIBLE_TAG;

/**
 * Handles tags.
 */
public class TagManager
{
    /**
     * List of tag options. Mods can just insert on this in mod constructor.
     */
    private static List<String> GLOBAL_TAG_OPTIONS = new ArrayList<>();

    /**
     * Block specific tag options.
     */
    private static Map<String, Predicate<IBlueprintDataProviderBE>> BLOCK_SPECIFIC_TAG_OPTIONS = new HashMap<>();

    static
    {
        GLOBAL_TAG_OPTIONS.add(GROUNDLEVEL_TAG);
        GLOBAL_TAG_OPTIONS.add(INVISIBLE_TAG);
    }
    /**
     * Register a new global tag option.
     * @param tag the option to register.
     */
    public static void registerGlobalTagOption(final String tag)
    {
        GLOBAL_TAG_OPTIONS.add(tag);
    }

    /**
     * Register a block specific tag option.
     * @param tag the tag to register.
     * @param predicate the predicate that has to apply.
     */
    public static void registerSpecificTagOption(final String tag, final Predicate<IBlueprintDataProviderBE> predicate)
    {
        BLOCK_SPECIFIC_TAG_OPTIONS.put(tag, predicate);
    }

    /**
     * Get all matching tag options for a block.
     * @param block the block to match.
     * @return collection of options.
     */
    public static Collection<String> getMatchingTagOptions(final IBlueprintDataProviderBE block)
    {
        return BLOCK_SPECIFIC_TAG_OPTIONS.entrySet().stream().filter(entry -> entry.getValue().test(block)).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * Get all global tag options.
     * @return the collection of global tag options.
     */
    public static Collection<String> getGlobalTagOptions()
    {
        return GLOBAL_TAG_OPTIONS;
    }
}
