package com.structurize.coremod.management;

import com.structurize.api.configuration.Configurations;
import com.structurize.api.util.ChangeStorage;
import com.structurize.api.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Singleton class that links colonies to minecraft.
 */
public final class Manager
{
    /**
     * Indicate if a schematic have just been downloaded.
     * Client only
     */
    private static          boolean schematicDownloaded = false;

    /**
     * List of the last changes to the world.
     */
    private static LinkedList<ChangeStorage> changeQueue = new LinkedList<>();

    /**
     * Pseudo unique id for the server
     */
    private static volatile UUID    serverUUID          = null;

    private Manager()
    {
        //Hides default constructor.
    }

    /**
     * Add a new item to the queue.
     * @param storage the storage to add.
     */
    public static void addToQueue(final ChangeStorage storage)
    {
        if (changeQueue.size() >= Configurations.gameplay.maxCachedChanges)
        {
            changeQueue.pop();
        }
        changeQueue.push(storage);
    }

    /**
     * Undo a change to the world made by a player.
     * @param player the player who made it.
     */
    public static void undo(final EntityPlayer player)
    {
        final Iterable<ChangeStorage> iterable = () -> changeQueue.descendingIterator();
        final Stream<ChangeStorage> storageStream = StreamSupport.stream(iterable.spliterator(), false);
        final Optional<ChangeStorage> theStorage = storageStream.filter(storage -> storage.isOwner(player)).findFirst();
        if (theStorage.isPresent())
        {
            theStorage.get().undo(player.getEntityWorld());
            changeQueue.remove(theStorage.get());
        }
    }

    /**
     * Get the Universal Unique ID for the server.
     *
     * @return the server Universal Unique ID for ther
     */
    public static UUID getServerUUID()
    {
        if (serverUUID == null)
        {
            return generateOrRetrieveUUID();
        }
        return serverUUID;
    }

    /**
     * Generate or retrieve the UUID of the server.
     * @return the UUID.
     */
    private static UUID generateOrRetrieveUUID()
    {
        final MapStorage storage = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getMapStorage();
        UUIDStorage instance = (UUIDStorage) storage.getOrLoadData(UUIDStorage.class, UUIDStorage.DATA_NAME);

        if (instance == null)
        {
            if (serverUUID == null)
            {
                Manager.setServerUUID(UUID.randomUUID());
                Log.getLogger().info(String.format("New Server UUID %s", serverUUID));
            }
            storage.setData(UUIDStorage.DATA_NAME, new UUIDStorage());
        }
        return serverUUID;
    }

    /**
     * Set the server UUID.
     *
     * @param uuid the universal unique id
     */
    public static void setServerUUID(final UUID uuid)
    {
        serverUUID = uuid;
    }

    /**
     * Whether or not a new schematic have been downloaded.
     *
     * @return True if a new schematic have been received.
     */
    public static boolean isSchematicDownloaded()
    {
        return schematicDownloaded;
    }

    /**
     * Set the schematic downloaded
     *
     * @param downloaded True if a new schematic have been received.
     */
    public static void setSchematicDownloaded(final boolean downloaded)
    {
        schematicDownloaded = downloaded;
    }
}
