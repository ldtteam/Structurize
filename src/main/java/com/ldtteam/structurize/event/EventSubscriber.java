package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.network.messages.StructurizeStylesMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Class with methods for receiving various forge events
 */
public class EventSubscriber
{
    /**
     * If the manager finished loading already.
     */
    private static boolean loaded = false;

    /**
     * Private constructor to hide implicit public one.
     */
    private EventSubscriber()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Called when world is about to load.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartingEvent event)
    {
        EntryPoint.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarting(@NotNull final WorldEvent.Load event)
    {
        if (!event.getWorld().isRemote())
        {
            if (!loaded)
            {
                Log.getLogger().warn("FMLServerStartedEvent");
                Structures.init();
                loaded = true;
            }
        }
    }

    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     * @param event {@link net.minecraftforge.event.entity.player.PlayerEvent}
     */
    @SubscribeEvent
    public static void onPlayerLogin(@NotNull final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), (ServerPlayerEntity) event.getPlayer());
            Network.getNetwork().sendToPlayer(new StructurizeStylesMessage(), (ServerPlayerEntity) event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }
        Manager.onWorldTick((ServerWorld) event.world);
    }
}
