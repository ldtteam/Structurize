package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.network.messages.StructurizeStylesMessage;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;

/**
 * Event handler used to catch various forge events.
 */
@Mod.EventBusSubscriber
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
        if (event.player instanceof PlayerEntityMP)
        {
            Structurize.getNetwork().sendTo(new ServerUUIDMessage(), (PlayerEntityMP) event.player);
            Structurize.getNetwork().sendTo(new StructurizeStylesMessage(), (PlayerEntityMP) event.player);
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


    /**
     * Event when a block is broken.
     * Event gets cancelled when there no permission to break a hut.
     *
     * @param event {@link net.minecraftforge.event.world.BlockEvent.BreakEvent}
     */
    @SubscribeEvent
    public void onBlockBreak(@NotNull final BlockEvent.BreakEvent event)
    {
        if (event.getPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem() == ModItems.scanTool)
        {
            final ItemStack itemstack = event.getPlayer().getHeldItem(EnumHand.MAIN_HAND);
            if (!itemstack.hasTagCompound())
            {
                itemstack.setTagCompound(new CompoundNBT());
            }
            final CompoundNBT compound = itemstack.getTagCompound();

            BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, event.getPos());
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "item.scepterSteel.point", event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            itemstack.setTagCompound(compound);

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }
        Manager.onWorldTick((WorldServer) event.world);
    }
}
