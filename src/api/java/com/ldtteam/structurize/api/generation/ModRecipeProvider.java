package com.ldtteam.structurize.api.generation;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider
{
    private static ModRecipeProvider instance;
    private final List<Consumer<Consumer<FinishedRecipe>>> recipes = new LinkedList<>();

    public ModRecipeProvider(final DataGenerator generatorIn)
    {
        super(generatorIn);
        instance = this;
    }

    @Override
    protected void buildShapelessRecipes(@NotNull final Consumer<FinishedRecipe> consumer)
    {
        recipes.forEach(builder -> builder.accept(consumer));
    }

    public void add(Consumer<Consumer<FinishedRecipe>> builder)
    {
        recipes.add(builder);
    }

    public static CriterionTriggerInstance getDefaultCriterion(ItemLike has)
    {
        return has(has);
    }

    public CriterionTriggerInstance getCriterion(ItemLike has)
    {
        return has(has);
    }

    public static ModRecipeProvider getInstance()
    {
        return instance;
    }
}
