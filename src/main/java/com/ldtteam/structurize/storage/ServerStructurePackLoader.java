package com.ldtteam.structurize.storage;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.util.StructureLoadingUtils;
import net.minecraft.Util;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ServerStructurePackLoader
{
    /**
     * The structure packs the server knows about.
     */
    public static Map<String, StructurePack> serverStructurePacks = new HashMap<>();


    @SubscribeEvent
    public static void onServerStarting(final ServerStartingEvent event)
    {
        final List<Path> modPaths = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods())
        {
            modPaths.add(mod.getOwningFile().getFile().findResource("structures", mod.getModId()));
        }

        Util.backgroundExecutor().execute(() ->
        {
            try
            {
                // This loads from the jar
                for (final Path modPath : modPaths)
                {
                    // Displaying the values
                    Files.list(modPath).forEach(element ->
                      {
                          Log.getLogger().info(element);
                          final Path packJsonPath = element.resolve("pack.json");
                          if (Files.exists(packJsonPath))
                          {
                              try
                              {
                                  try (final JsonReader reader = new JsonReader(Files.newBufferedReader(packJsonPath)))
                                  {
                                      final StructurePack pack = new StructurePack(Streams.parse(reader).getAsJsonObject(), element);
                                      serverStructurePacks.put(pack.getName(), pack);
                                      Log.getLogger().warn("Registered structure pack: " + pack.getName());
                                  }
                              }
                              catch (final IOException ex)
                              {
                                  Log.getLogger().warn("Error Reading pack: ", ex);
                              }
                          }
                      }
                      );
                }

                

                //todo now we want to also load from the game main folder (here we would also then store the ones we downloaded).
                //todo those in the jar, are NEVER updated from the server!
                //todo those in the folder are double checked from the server
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }
}
