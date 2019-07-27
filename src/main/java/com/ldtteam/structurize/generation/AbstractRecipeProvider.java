package com.ldtteam.structurize.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract class for creating Recipes.
 */
public abstract class AbstractRecipeProvider implements IDataProvider
{
    /**
     * Create a shaped Recipe.
     *
     * @param resultItem The ResourceLocation of the item being crafted.
     * @param resultCount The amount of the item being crafted.
     * @param pattern1 The first row of the recipe's pattern. MUST BE 3 CHARACTERS.
     * @param pattern2 The second row of the recipe's pattern. MUST BE 3 CHARACTERS.
     * @param pattern3 The third row of the recipe's pattern. MUST BE 3 CHARACTERS.
     * @param ingredients The ingredients used in the recipe.
     * @return The recipe JSON created.
     */
    protected JsonObject createShapedRecipe(final ResourceLocation resultItem, final int resultCount, final String pattern1, final String pattern2, final String pattern3, final ShapedIngredient... ingredients)
    {

        if (pattern1.length() != 3 || pattern2.length() != 3 || pattern3.length() != 3)
            throw new IllegalArgumentException();

        final JsonObject recipe = new JsonObject();

        recipe.addProperty("type", "minecraft:crafting_shaped");

        setRecipeResult(recipe, resultItem, resultCount);

        final JsonArray recipePattern = new JsonArray();

        recipePattern.add(pattern1);
        recipePattern.add(pattern2);
        recipePattern.add(pattern3);

        recipe.add("pattern", recipePattern);

        for (ShapedIngredient ingredient : ingredients)
        {
            addShapedIngredient(recipe, ingredient.getType(), ingredient.getKey(), ingredient.getLocation());
        }

        return recipe;
    }

    /**
     * Create a shapeless Recipe.
     *
     * @param resultItem The ResourceLocation of the item being crafted.
     * @param resultCount The amount of the item being crafted.
     * @param ingredients The ingredients used to craft the item. You may use the same ingredient multiple times to increase the amount needed. (e.g. 2 dye, one wool)
     * @return The recipe JSON created.
     */
    protected JsonObject createShaplessRecipe(final ResourceLocation resultItem, final int resultCount, final ShaplessIngredient... ingredients)
    {
        if (ingredients.length < 1 || ingredients.length > 9)
            throw new IllegalArgumentException();

        final JsonObject recipe = new JsonObject();

        recipe.addProperty("type", "minecraft:crafting_shapeless");

        setRecipeResult(recipe, resultItem, resultCount);

        for (ShaplessIngredient ingredient : ingredients)
        {
            addShaplessIngredient(recipe, ingredient.getType(), ingredient.getLocation());
        }

        return recipe;
    }

    /**
     * Add an ingredient to a shaped recipe.
     *
     * @param recipeJson The recipeJson to add the ingredient to.
     * @param type The type of the ingredient provided.
     * @param keyName The singleCharacter key name. (also present in the recipe's pattern)
     * @param ingredient The ResourceLocation of the item/tag.
     */
    private void addShapedIngredient(final JsonObject recipeJson, final String type, final String keyName, final ResourceLocation ingredient)
    {
        if (keyName.length() != 1)
            throw new IllegalArgumentException();

        final JsonObject keyObject = new JsonObject();

        keyObject.addProperty(type, ingredient.toString());

        if (recipeJson.getAsJsonObject("key") == null)
            recipeJson.add("key", new JsonObject());

        recipeJson.getAsJsonObject("key").add(keyName, keyObject);

    }

    /**
     * Add an ingredient to a shapeless recipe.
     * @param recipeJson The recipeJson to add the ingredient to.
     * @param type The type of the ingredient provided.
     * @param ingredient The ResourceLocation of the item/tag.
     */
    private void addShaplessIngredient(final JsonObject recipeJson, final String type, final ResourceLocation ingredient)
    {
        final JsonObject ingredientObject = new JsonObject();

        ingredientObject.addProperty(type, ingredient.toString());

        if (recipeJson.getAsJsonArray("ingredients") == null)
            recipeJson.add("ingredients", new JsonArray());

        recipeJson.getAsJsonArray("ingredients").add(ingredientObject);
    }

    /**
     * Set the group of a recipeJson.
     * This is used in the RecipeBook GUI to group items together.
     * E.g dyed_wool group would have all the dyed_wool items under a single icon.
     *
     * @param recipeJson The recipeJson to set the group on.
     * @param group The name of the group to set.
     */
    protected void setRecipeGroup(final JsonObject recipeJson, final String group)
    {
        recipeJson.addProperty("group", group);
    }

    /**
     * Set the result item for a recipe.
     *
     * @param recipeJson The recipeJson to set the result on.
     * @param outputItem The ResourceLocation of the result.
     * @param count The amount of the result.
     */
    private void setRecipeResult(final JsonObject recipeJson, final ResourceLocation outputItem, final int count)
    {
        final JsonObject resultOject = new JsonObject();

        if (count > 1)
        {
            resultOject.addProperty("count", count);
        }
        resultOject.addProperty("item", outputItem.toString());

        recipeJson.add("result", resultOject);
    }

    /**
     * Used to create a shapeless ingredient for a shapless recipe.
     */
    public class ShaplessIngredient
    {

        private final String type;
        private final ResourceLocation location;

        public ShaplessIngredient(final String type, final ResourceLocation location)
        {
            this.type = type;
            this.location = location;
        }

        public String getType()
        {
            return this.type;
        }

        public ResourceLocation getLocation()
        {
            return this.location;
        }
    }

    /**
     * Used to create a shaped ingredient for a shaped recipe.
     */
    public class ShapedIngredient
    {
        private final String type;
        private final String key;
        private final ResourceLocation location;

        public ShapedIngredient(final String type, final String key, final ResourceLocation location)
        {
            this.type = type;
            this.key = key;
            this.location = location;
        }

        public String getType()
        {
            return this.type;
        }

        public String getKey()
        {
            return this.key;
        }

        public ResourceLocation getLocation()
        {
            return this.location;
        }
    }
}
