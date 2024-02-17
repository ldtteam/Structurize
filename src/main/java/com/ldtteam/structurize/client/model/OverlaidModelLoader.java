package com.ldtteam.structurize.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

/**
 * Simple loader to create {@link OverlaidGeometry}.
 */
public class OverlaidModelLoader implements IGeometryLoader<OverlaidGeometry>
{
    @NotNull
    @Override
    public OverlaidGeometry read(@NotNull JsonObject jsonObject,
                                 @NotNull JsonDeserializationContext deserializationContext) throws JsonParseException
    {
        final String parent = jsonObject.get("parent").getAsString();
        final ResourceLocation parentLocation = new ResourceLocation(parent);

        return new OverlaidGeometry(parentLocation);
    }
}
