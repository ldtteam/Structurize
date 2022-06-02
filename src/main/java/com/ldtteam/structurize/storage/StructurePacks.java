package com.ldtteam.structurize.storage;

import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that contains all the structurepacks of the instance.
 */
public class StructurePacks
{
    /**
     * Current pack format.
     * Increase when the pack data format changes, or minecraft version changes require a full schematic update.
     * When the pack format doesn't align, the pack won't be loaded.
     */
    public static final int PACK_FORMAT = 1;

    /**
     * The list of registered structure packs.
     * This might be accessed concurrently by client/server. That's why it is a concurrent hashmap.
     */
    public static Map<String, StructurePack> packMetas = new ConcurrentHashMap<>();

    // todo We now need a way to get a blueprint, given
    //  a) The StructurePack ID and
    //  b) The path within the pack. and we need this "transparent" for client/server.

    /**
     * Discover a structure pack at a given path.
     * @param element the path to check for.
     * @param immutable if jar (true), else false.
     * @param modList the list of mods loaded on this instance.
     */
    public static void discoverPackAtPath(final Path element, final boolean immutable, final List<String> modList)
    {
        final Path packJsonPath = element.resolve("pack.json");
        if (Files.exists(packJsonPath))
        {
            try (final JsonReader reader = new JsonReader(Files.newBufferedReader(packJsonPath)))
            {
                final StructurePack pack = new StructurePack(Streams.parse(reader).getAsJsonObject(), element);
                if (pack.getPackFormat() == PACK_FORMAT)
                {
                    pack.setImmutable(immutable);
                    for (final String modId : pack.getModList())
                    {
                        if (!modList.contains(modId))
                        {
                            Log.getLogger().warn("Missing Mod: " + modId + " for Pack: " + pack.getName());
                            return;
                        }
                    }
                    packMetas.put(pack.getName(), pack);
                    Log.getLogger().info("Registered structure pack: " + pack.getName());
                }
                else
                {
                    Log.getLogger().warn("Wrong Pack Format: " + pack.getName());
                }
            }
            catch (final IOException ex)
            {
                Log.getLogger().warn("Error Reading pack: ", ex);
            }
        }
    }
}
