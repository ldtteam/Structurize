package com.ldtteam.structurize.client;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Registry and handler for modifying blueprint information with regards to entities.
 */
public class BlueprintEntityInfoTransformHandler
{
    private static BlueprintEntityInfoTransformHandler ourInstance = new BlueprintEntityInfoTransformHandler();

    public static BlueprintEntityInfoTransformHandler getInstance()
    {
        return ourInstance;
    }

    private Map<Predicate<CompoundTag>, Function<CompoundTag, CompoundTag>> entityInfoTransformHandler = new HashMap<>();

    private BlueprintEntityInfoTransformHandler()
    {
    }

    /**
     * Method to add a transformer.
     *
     * @param transformPredicate The predicate to check if this transform function needs to be applied.
     * @param transformHandler The tranformer.
     */
    public void AddTransformHandler(final Predicate<CompoundTag> transformPredicate, final Function<CompoundTag, CompoundTag> transformHandler)
    {
        entityInfoTransformHandler.put(transformPredicate, transformHandler);
    }

    /**
     * Process a entityinfo. Checks all known transformers and applies the first it finds.
     *
     * @param entityInfo The entity info to transform
     * @return The transformed entityinfo.
     */
    public CompoundTag Transform(final CompoundTag entityInfo)
    {
        return getTransformHandler(entityInfo).apply(entityInfo);
    }

    private Function<CompoundTag,CompoundTag> getTransformHandler(final CompoundTag entityInfo)
    {
        return entityInfoTransformHandler.keySet().stream().filter(p -> p.test(entityInfo)).findFirst().map(p -> entityInfoTransformHandler.get(p)).orElse(Function.identity());
    }
}
