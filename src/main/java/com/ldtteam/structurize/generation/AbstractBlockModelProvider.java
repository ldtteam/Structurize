package com.ldtteam.structurize.generation;

import com.google.gson.JsonObject;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;

/**
 * Abstract class for creating Block Model jsons.
 * Only contains what was needed when created.
 */
public abstract class AbstractBlockModelProvider implements IDataProvider
{

    /**
     * Take a Model's Json, and switch the texture by the name of textureName with textureLocation.
     *
     * @param jsonModel The Json for the model you're editing.
     * @param textureName The name of the texture in the model json.
     * @param textureLocation The new resource location for the texture.
     */
    protected void swapModelTexture(final JsonObject jsonModel, final String textureName, final ResourceLocation textureLocation)
    {
        if (jsonModel.getAsJsonObject("textures") == null)
            jsonModel.add("textures", new JsonObject());

        jsonModel.getAsJsonObject("textures").addProperty(textureName, textureLocation.toString());
    }

}
