package com.ldtteam.structurize.storage.rendering;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.config.BlueprintRenderSettings;
import com.ldtteam.structurize.network.messages.SyncPreviewCacheToClient;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;

import static com.ldtteam.structurize.api.util.constant.Constants.DISPLAY_SHARED;

/**
 * Class handling blueprint syncing between players.
 */
public class ServerPreviewDistributor
{
    /**
     * Players that signed up to receive blueprint data.
     */
    public static Map<UUID, Tuple<ServerPlayer, BlueprintRenderSettings>> registeredPlayers = new HashMap<>();

    @SubscribeEvent
    public static void onLogin(final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer().level.isClientSide)
        {
            return;
        }
        registeredPlayers.put(event.getPlayer().getUUID(), new Tuple<>((ServerPlayer) event.getPlayer(), BlueprintRenderSettings.instance));
    }

    @SubscribeEvent
    public static void onLogout(final PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getPlayer().level.isClientSide)
        {
            return;
        }
        registeredPlayers.remove(event.getPlayer().getUUID());
    }

    /**
     * Distribute this rendering cache to all that are wanting to listen.
     * @param renderingCache the cache to distribute.
     */
    public static void distribute(final BlueprintPreviewData renderingCache, final Player player)
    {
        for (final Tuple<ServerPlayer, BlueprintRenderSettings> entry : registeredPlayers.values())
        {
            if (entry.getB().renderSettings.get(DISPLAY_SHARED) && entry.getA().isAlive() && player.level == entry.getA().level && !player.getUUID().equals(entry.getA().getUUID()) && (entry.getA().blockPosition().distSqr(renderingCache.pos) < 128 * 128 || renderingCache.pos.equals(BlockPos.ZERO)))
            {
                Network.getNetwork().sendToPlayer(new SyncPreviewCacheToClient(renderingCache, entry.getA().getUUID().toString()), entry.getA());
            }
        }
    }
}
