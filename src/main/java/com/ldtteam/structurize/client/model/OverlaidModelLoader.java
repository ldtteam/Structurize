package com.ldtteam.structurize.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class OverlaidModelLoader implements UnbakedModelLoader<OverlaidUnbakedModel>
{
    @Override
    public OverlaidUnbakedModel read(final JsonObject jsonObject, final JsonDeserializationContext context)
        throws JsonParseException
    {
        return new OverlaidUnbakedModel(Identifier.parse(jsonObject.get("parent").getAsString()));
    }
}
