package com.ldtteam.structurize.storage.rendering;

import com.ldtteam.structurize.network.messages.SyncPreviewCacheToClient;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.UUID;

/**
 * Class handling blueprint syncing between players.
 */
public class ServerPreviewDistributor
{
    /**
     * Players that signed up to receive blueprint data.
     */
    private static Object2BooleanMap<UUID> registeredPlayers = new Object2BooleanOpenHashMap<>();

    @SubscribeEvent
    public static void onLogout(final PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity().level().isClientSide)
        {
            RenderingCache.clear();
            return;
        }
        registeredPlayers.removeBoolean(event.getEntity().getUUID());
    }

    /**
     * Distribute this rendering cache to all that are wanting to listen.
     * @param renderingCache the cache to distribute.
     */
    public static void distribute(final BlueprintPreviewData renderingCache, final ServerPlayer sourcePlayer)
    {
        for (final ServerPlayer player : sourcePlayer.getServer().getLevel(sourcePlayer.level().dimension()).players())
        {
            if ((player.blockPosition().distSqr(renderingCache.getPos()) < 128 * 128 || renderingCache.getPos().equals(BlockPos.ZERO)) && // within sensible distance
                !player.getUUID().equals(sourcePlayer.getUUID()) && // dont send to source
                player.isAlive() && // dont send to dead
                registeredPlayers.getBoolean(player.getUUID())) // only those who want to see previews
            {
                new SyncPreviewCacheToClient(renderingCache, player.getUUID()).sendToPlayer(player);
            }
        }
    }

    /**
     * Register a player with their settings.
     * @param player the player.
     * @param displayShared if displayed is shared or not.
     */
    public static void register(final ServerPlayer player, final boolean displayShared)
    {
        registeredPlayers.put(player.getUUID(), displayShared);
    }
}
