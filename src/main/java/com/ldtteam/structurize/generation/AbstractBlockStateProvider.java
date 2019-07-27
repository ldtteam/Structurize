package com.ldtteam.structurize.generation;

import com.google.gson.JsonObject;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract class for creating Block State jsons.
 * Only contains what was needed when created.
 */
public abstract class AbstractBlockStateProvider implements IDataProvider
{
    /**
     * Add a variant Json object to the Variants of a blockstate json.
     *
     * @param blockstateJson The blockstate json to add the variant to.
     * @param variantJson The variant to add.
     * @param key The key for the variant.
     */
    protected void addVariantToVariants(final JsonObject blockstateJson, final JsonObject variantJson, final String key)
    {
        if (blockstateJson.getAsJsonObject("variants") == null)
            blockstateJson.add("variants", new JsonObject());

        blockstateJson.getAsJsonObject("variants").add(key, variantJson);
    }

    /**
     * Set the X rotation value of a variant Json object.
     *
     * @param variantJson The variant Json to set the X rotation on.
     * @param x The X rotation to set
     */
    protected void setVariantX(final JsonObject variantJson, final int x)
    {
        variantJson.addProperty("x", x);
    }

    /**
     * Add an Y rotation value of a variant Json object.
     *
     * @param variantJson The variant Json to set the Y rotation on.
     * @param y The Y rotation to set
     */
    protected void setVariantY(final JsonObject variantJson, final int y)
    {
        variantJson.addProperty("y", y);
    }

    /**
     * Set the model value of a variant Json object.
     *
     * @param variantJson The variant Json to set the model on.
     * @param modelLocation The model value to set
     */
    protected void setVariantModel(final JsonObject variantJson, final ResourceLocation modelLocation)
    {
        variantJson.addProperty("model", modelLocation.toString());
    }
}
