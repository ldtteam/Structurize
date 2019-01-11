package com.structurize.structures.client;

import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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

    public void AddTransformHandler(@NotNull final Predicate<Template.EntityInfo> transformPredicate, @NotNull final Function<Template.EntityInfo, Template.EntityInfo> transformHandler)
    {
        entityInfoTransformHandler.put(transformPredicate, transformHandler);
    }

    public Template.EntityInfo Transform(@NotNull final Template.EntityInfo entityInfo)
    {
        return getTransformHandler(entityInfo).apply(entityInfo);
    }

    private Function<Template.EntityInfo, Template.EntityInfo> getTransformHandler(@NotNull final Template.EntityInfo entityInfo)
    {
        return entityInfoTransformHandler.keySet().stream().filter(p -> p.test(entityInfo)).findFirst().map(p -> entityInfoTransformHandler.get(p)).orElse(Function.identity());
    }
}
