package com.ldtteam.structurize.client;

import com.ldtteam.structurize.util.BlockInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Registry and handler for modifying blueprint information with regards to blocks.
 */
public class BlueprintBlockInfoTransformHandler
{
    private static BlueprintBlockInfoTransformHandler ourInstance = new BlueprintBlockInfoTransformHandler();

    public static BlueprintBlockInfoTransformHandler getInstance()
    {
        return ourInstance;
    }

    private Map<Predicate<BlockInfo>, Function<BlockInfo, BlockInfo>> blockInfoTransformHandler = new HashMap<>();

    private BlueprintBlockInfoTransformHandler()
    {
    }

    /**
     * Method to add a transformer.
     *
     * @param transformPredicate The predicate to check if this transform function needs to be applied.
     * @param transformHandler The tranformer.
     */
    public void AddTransformHandler(final Predicate<BlockInfo> transformPredicate, final Function<BlockInfo, BlockInfo> transformHandler)
    {
        blockInfoTransformHandler.put(transformPredicate, transformHandler);
    }

    /**
     * Process a blockinfo. Checks all known transformers and applies the first it finds.
     *
     * @param blockInfo The block info to transform
     * @return The transformed blockinfo.
     */
    public BlockInfo Transform(final BlockInfo blockInfo)
    {
        return getTransformHandler(blockInfo).apply(blockInfo);
    }

    private Function<BlockInfo, BlockInfo> getTransformHandler(final BlockInfo blockInfo)
    {
        return blockInfoTransformHandler.keySet().stream().filter(p -> p.test(blockInfo)).findFirst().map(p -> blockInfoTransformHandler.get(p)).orElse(Function.identity());
    }
}
