package com.ldtteam.structures.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
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

    private Map<Predicate<NBTTagCompound>, Function<NBTTagCompound, NBTTagCompound>> entityInfoTransformHandler = new HashMap<>();

    private BlueprintEntityInfoTransformHandler()
    {
    }

    /**
     * Method to add a transformer.
     *
     * @param transformPredicate The predicate to check if this transform function needs to be applied.
     * @param transformHandler The tranformer.
     */
    public void AddTransformHandler(@NotNull final Predicate<NBTTagCompound> transformPredicate, @NotNull final Function<NBTTagCompound, NBTTagCompound> transformHandler)
    {
        entityInfoTransformHandler.put(transformPredicate, transformHandler);
    }

    /**
     * Process a entityinfo. Checks all known transformers and applies the first it finds.
     *
     * @param entityInfo The entity info to transform
     * @return The transformed entityinfo.
     */
    public NBTTagCompound Transform(@NotNull final NBTTagCompound entityInfo)
    {
        return getTransformHandler(entityInfo).apply(entityInfo);
    }

    private Function<NBTTagCompound,NBTTagCompound> getTransformHandler(@NotNull final NBTTagCompound entityInfo)
    {
        return entityInfoTransformHandler.keySet().stream().filter(p -> p.test(entityInfo)).findFirst().map(p -> entityInfoTransformHandler.get(p)).orElse(Function.identity());
    }
}
