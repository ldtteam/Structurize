package com.structurize.coremod.event;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.Structurize;
import com.structurize.coremod.network.messages.ServerUUIDMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event handler used to catch various forge events.
 */
public class FMLEventHandler
{
    /**
     * Called when a player logs in. If the joining player is a MP-Player, sends
     * all possible styles in a message.
     *
     * @param event {@link net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent}
     */
    @SubscribeEvent
    public void onPlayerLogin(@NotNull final PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            Structurize.getNetwork().sendTo(new ServerUUIDMessage(), (EntityPlayerMP) event.player);
        }
    }

    /**
     * Called when the config is changed, used to synch between file and game.
     *
     * @param event the on config changed event.
     */
    @SubscribeEvent
    public void onConfigChanged(@NotNull final ConfigChangedEvent.OnConfigChangedEvent event)
    {
        ConfigManager.sync(Constants.MOD_ID, Config.Type.INSTANCE);
    }
}
