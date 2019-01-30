package com.ldtteam.structures.client;

import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Registry and handler for modifying template information with regards to entities.
 */
public class TemplateEntityInfoTransformHandler
{
    private static TemplateEntityInfoTransformHandler ourInstance = new TemplateEntityInfoTransformHandler();

    public static TemplateEntityInfoTransformHandler getInstance()
    {
        return ourInstance;
    }

    private Map<Predicate<Template.EntityInfo>, Function<Template.EntityInfo, Template.EntityInfo>> entityInfoTransformHandler = new HashMap<>();

    private TemplateEntityInfoTransformHandler()
    {
    }

    /**
     * Method to add a transformer.
     *
     * @param transformPredicate The predicate to check if this transform function needs to be applied.
     * @param transformHandler The tranformer.
     */
    public void AddTransformHandler(@NotNull final Predicate<Template.EntityInfo> transformPredicate, @NotNull final Function<Template.EntityInfo, Template.EntityInfo> transformHandler)
    {
        entityInfoTransformHandler.put(transformPredicate, transformHandler);
    }

    /**
     * Process a entityinfo. Checks all known transformers and applies the first it finds.
     *
     * @param entityInfo The entity info to transform
     * @return The transformed entityinfo.
     */
    public Template.EntityInfo Transform(@NotNull final Template.EntityInfo entityInfo)
    {
        return getTransformHandler(entityInfo).apply(entityInfo);
    }

    private Function<Template.EntityInfo, Template.EntityInfo> getTransformHandler(@NotNull final Template.EntityInfo entityInfo)
    {
        return entityInfoTransformHandler.keySet().stream().filter(p -> p.test(entityInfo)).findFirst().map(p -> entityInfoTransformHandler.get(p)).orElse(Function.identity());
    }
}
