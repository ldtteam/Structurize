package com.ldtteam.structurize.management.schemaserver.utils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BetterGSON
{
    public static enum Additions
    {
        PATH(gb -> gb.registerTypeHierarchyAdapter(Path.class, PathSerializer.INSTANCE)),
        FORMAT(GsonBuilder::setPrettyPrinting);

        private final Function<GsonBuilder, GsonBuilder> additionFactory;

        private Additions(final Function<GsonBuilder, GsonBuilder> additionFactory)
        {
            this.additionFactory = additionFactory;
        }
    }

    public static GsonBuilder with(final Additions... additions)
    {
        GsonBuilder result = new GsonBuilder();

        for (final Additions addition : additions)
        {
            result = addition.additionFactory.apply(result);
        }

        return result;
    }

    public static Gson withCreate(final Additions... additions)
    {
        return with(additions).create();
    }

    private static class PathSerializer implements JsonDeserializer<Path>, JsonSerializer<Path>
    {
        private static final PathSerializer INSTANCE = new PathSerializer();

        @Override
        public Path deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException
        {
            return Paths.get(jsonElement.getAsString());
        }

        @Override
        public JsonElement serialize(final Path path, final Type type, final JsonSerializationContext jsonSerializationContext)
        {
            return new JsonPrimitive(path.toAbsolutePath().toString());
        }
    }
}
