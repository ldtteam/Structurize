package com.ldtteam.structurize.generation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract class for creating Item Model jsons.
 */
public abstract class AbstractItemModelProvider implements IDataProvider
{

    /**
     * Set the parent of an itemModel (such as a blockModel json).
     *
     * @param modelJson The itemModel's JSON.
     * @param parent The resource location of the Parent model.
     */
    protected void setModelParent(final JsonObject modelJson, final ResourceLocation parent)
    {
        modelJson.addProperty("parent", parent.toString());
    }

    /**
     * Add a state to the itemModel.
     *
     * @param modelJson The itemModel's JSON.
     * @param stateJson The state's JSON.
     * @param name The name of the state.
     */
    protected void addState(final JsonObject modelJson, final JsonObject stateJson, final String name)
    {
        if (modelJson.getAsJsonObject("display") == null)
            modelJson.add("display", new JsonObject());

        modelJson.getAsJsonObject("display").add(name, stateJson);
    }

    /**
     * Set the rotation for a state.
     *
     * @param stateJson The state's JSON.
     * @param x The X rotation value.
     * @param y The Y rotation value.
     * @param z The Z rotation value.
     */
    protected void setRotationForState(final JsonObject stateJson, final int x, final int y, final int z)
    {
        final JsonArray rotationArray = new JsonArray();

        rotationArray.add(x);
        rotationArray.add(y);
        rotationArray.add(z);

        stateJson.add("rotation", rotationArray);
    }

    /**
     * Set the translation for a state.
     *
     * @param stateJson The state's JSON.
     * @param x The X translation value.
     * @param y The Y translation value.
     * @param z The Z translation value.
     */
    protected void setTranslationForState(final JsonObject stateJson, final int x, final int y, final int z)
    {
        final JsonArray rotationArray = new JsonArray();

        rotationArray.add(x);
        rotationArray.add(y);
        rotationArray.add(z);

        stateJson.add("translation", rotationArray);
    }

    /**
     * Set the scale for a state.
     *
     * @param stateJson The state's JSON.
     * @param x The X scale value.
     * @param y the Y scale value.
     * @param z The Z scale value.
     */
    protected void setScaleForState(final JsonObject stateJson, final double x, final double y, final double z)
    {
        final JsonArray rotationArray = new JsonArray();

        rotationArray.add(x);
        rotationArray.add(y);
        rotationArray.add(z);

        stateJson.add("scale", rotationArray);
    }

}
