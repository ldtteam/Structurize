package com.ldtteam.structurize.api.generation;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.util.IItemProvider;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider
{
    private static ModRecipeProvider instance;
    private final List<Consumer<Consumer<IFinishedRecipe>>> recipes = new LinkedList<>();

    public ModRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
        instance = this;
    }

    @Override
    protected void buildShapelessRecipes(@NotNull final Consumer<IFinishedRecipe> consumer)
    {
        recipes.forEach(builder -> builder.accept(consumer));
    }

    public void add(Consumer<Consumer<IFinishedRecipe>> builder)
    {
        recipes.add(builder);
    }

    public static ICriterionInstance getDefaultCriterion(IItemProvider has)
    {
        return has(has);
    }

    public ICriterionInstance getCriterion(IItemProvider has)
    {
        return has(has);
    }

    public static ModRecipeProvider getInstance()
    {
        return instance;
    }
}
