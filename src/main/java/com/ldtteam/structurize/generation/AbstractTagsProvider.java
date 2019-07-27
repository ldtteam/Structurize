package com.ldtteam.structurize.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract class to create a tag.
 */
public abstract class AbstractTagsProvider implements IDataProvider
{

    /**
     * Create a new tag JSON.
     *
     * @param replace Sets the replace value of a JSON.
     * @param locations The ResourceLocations of the items within this tag.
     * @return The new tag JSON created.
     */
    protected final JsonObject createTagJson(final boolean replace, final ResourceLocation... locations)
    {
        final JsonObject tagJson = new JsonObject();

        setReplaceValue(tagJson, replace);

        for (ResourceLocation location : locations)
        {
            addValue(tagJson, location);
        }

        return tagJson;
    }

    /**
     * Set the replace value of a tagJson.
     *
     * @param tagJson The tagJson to set the replace value on.
     * @param replace The replace value to set.
     */
    private void setReplaceValue(final JsonObject tagJson, final boolean replace)
    {
        tagJson.addProperty("replace", replace);
    }

    /**
     * Add a ResourceLocation to a tagJson.
     *
     * @param tagJson The tagJson to add the ResourceLocation to.
     * @param location The ResourceLocation to add.
     */
    private void addValue(final JsonObject tagJson, final ResourceLocation location)
    {
        if (tagJson.getAsJsonArray("values") == null)
            tagJson.add("values", new JsonArray());

        tagJson.getAsJsonArray("values").add(location.toString());
    }

}
