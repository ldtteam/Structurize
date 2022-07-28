package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.commands.EntryPoint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.network.messages.StructurizeStylesMessage;
import com.ldtteam.structurize.util.BackUpHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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

    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent event)
    {
        Structures.init();
        BackUpHelper.loadLinkSessionManager();
    }

    @SubscribeEvent
    public static void onServerStopping(final ServerStoppingEvent event)
    {
        BackUpHelper.saveLinkSessionManager();
    }

    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     * @param event {@link net.minecraftforge.event.entity.player.PlayerEvent}
     */
    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer serverPlayer)
        {
            Network.getNetwork().sendToPlayer(new ServerUUIDMessage(), serverPlayer);
            Network.getNetwork().sendToPlayer(new StructurizeStylesMessage(), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(final TickEvent.LevelTickEvent event)
    {
        if (event.level instanceof ServerLevel serverLevel)
        {
            if (event.phase == Phase.START)
            {
                Manager.onWorldTick(serverLevel);
            }
        }
    }
}
