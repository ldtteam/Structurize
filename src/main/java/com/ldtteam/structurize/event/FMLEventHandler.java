package com.ldtteam.structurize.event;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.network.messages.ServerUUIDMessage;
import com.ldtteam.structurize.network.messages.StructurizeStylesMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.world.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            Structurize.getNetwork().sendToPlayer(new ServerUUIDMessage(), (ServerPlayerEntity) event.getPlayer());
            Structurize.getNetwork().sendToPlayer(new StructurizeStylesMessage(), (ServerPlayerEntity) event.getPlayer());
        }
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
        if (event.getPlayer().getHeldItem(Hand.MAIN_HAND).getItem() == ModItems.scanTool)
        {
            final ItemStack itemstack = event.getPlayer().getHeldItem(Hand.MAIN_HAND);
            if (!itemstack.hasTag())
            {
                itemstack.setTag(new CompoundNBT());
            }
            final CompoundNBT compound = itemstack.getTag();

            BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, event.getPos());
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "item.scepterSteel.point", event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            itemstack.setTag(compound);

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
        Manager.onWorldTick((ServerWorld) event.world);
    }
}
