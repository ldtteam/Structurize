package com.ldtteam.structurize.util;

import com.ldtteam.structurize.blocks.interfaces.IAnchorBlock;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDLEVEL_TAG;
import static com.ldtteam.structurize.api.util.constant.Constants.INVISIBLE_TAG;

/**
 * Handles tags.
 */
public class TagManager
{
    /**
     * List of tag options. Mods can just insert on this in mod constructor.
     */
    private static Set<String> GLOBAL_TAG_OPTIONS = new HashSet<>();

    /**
     * Block specific tag options.
     */
    private static Map<String, Predicate<IAnchorBlock>> BLOCK_SPECIFIC_TAG_OPTIONS = new HashMap<>();

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
    public static void registerSpecificTagOption(final String tag, final Predicate<IAnchorBlock> predicate)
    {
        final Predicate<IAnchorBlock> storedPredicate = BLOCK_SPECIFIC_TAG_OPTIONS.getOrDefault(tag, null);
        if (storedPredicate == null)
        {
            BLOCK_SPECIFIC_TAG_OPTIONS.put(tag, predicate);
        }
        else
        {
            BLOCK_SPECIFIC_TAG_OPTIONS.put(tag, storedPredicate.or(predicate));
        }
    }

    /**
     * Get all matching tag options for a block.
     * @param block the block to match.
     * @return collection of options.
     */
    public static Collection<String> getMatchingTagOptions(final IAnchorBlock block)
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
