package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.IOPool;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Class with methods for receiving various forge events
 */
public class EventSubscriber
{
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
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        EntryPoint.register(event.getDispatcher(), event.getCommandSelection());
    }

    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     * @param event {@link net.neoforged.neoforge.event.entity.player.PlayerEvent}
     */
    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent event)
    {
        if (event.level instanceof ServerLevel serverLevel)
        {
            if (event.phase == Phase.START)
            {
                BlockUtils.checkOrInit();
                Manager.onWorldTick(serverLevel);
            }
        }
        else if (event.level instanceof ClientLevel)
        {
            BlockUtils.checkOrInit();
        }
    }

    @SubscribeEvent
    public static void onServerStopped(@NotNull final ServerStoppingEvent event)
    {
        IOPool.shutdown();
    }
}
