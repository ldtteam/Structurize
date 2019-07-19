package com.ldtteam.structures.client;

import net.minecraft.nbt.CompoundNBT;
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

    private Map<Predicate<CompoundNBT>, Function<CompoundNBT, CompoundNBT>> entityInfoTransformHandler = new HashMap<>();

    private BlueprintEntityInfoTransformHandler()
    {
    }

    /**
     * Method to add a transformer.
     *
     * @param transformPredicate The predicate to check if this transform function needs to be applied.
     * @param transformHandler The tranformer.
     */
    public void AddTransformHandler(@NotNull final Predicate<CompoundNBT> transformPredicate, @NotNull final Function<CompoundNBT, CompoundNBT> transformHandler)
    {
        entityInfoTransformHandler.put(transformPredicate, transformHandler);
    }

    /**
     * Process a entityinfo. Checks all known transformers and applies the first it finds.
     *
     * @param entityInfo The entity info to transform
     * @return The transformed entityinfo.
     */
    public CompoundNBT Transform(@NotNull final CompoundNBT entityInfo)
    {
        return getTransformHandler(entityInfo).apply(entityInfo);
    }

    private Function<CompoundNBT,CompoundNBT> getTransformHandler(@NotNull final CompoundNBT entityInfo)
    {
        return entityInfoTransformHandler.keySet().stream().filter(p -> p.test(entityInfo)).findFirst().map(p -> entityInfoTransformHandler.get(p)).orElse(Function.identity());
    }
}
