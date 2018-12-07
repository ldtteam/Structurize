package com.structurize.coremod.event;

import com.structurize.api.util.BlockPosUtil;
import com.structurize.api.util.LanguageHandler;
import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.Structurize;
import com.structurize.coremod.blocks.decorative.BlockTimberFrame;
import com.structurize.coremod.blocks.types.TimberFrameType;
import com.structurize.coremod.items.ModItems;
import com.structurize.coremod.management.Manager;
import com.structurize.coremod.network.messages.StructurizeStylesMessage;
import com.structurize.coremod.network.messages.ServerUUIDMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;

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
            Structurize.getNetwork().sendTo(new StructurizeStylesMessage(), (EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onItemRegistryMissingMappings(final RegistryEvent.MissingMappings<Item> event)
    {
        onRegistryMissingMappings(event);
    }

    @SubscribeEvent
    public void onBlockRegistryMissingMappings(final RegistryEvent.MissingMappings<Block> event)
    {
        onRegistryMissingMappings(event);
    }

    private static <T extends IForgeRegistryEntry<T>> void onRegistryMissingMappings(final RegistryEvent.MissingMappings<T> event)
    {
        event.getAllMappings().forEach(missingMapping -> {
            if (missingMapping.key.getNamespace().equals(Constants.MINECOLONIES_MOD_ID))
            {
                final String path = missingMapping.key.getPath();
                final ResourceLocation remappedTargetId = new ResourceLocation(Constants.MOD_ID.toLowerCase() + ":" + path);
                @Nullable final T target = missingMapping.registry.getValue(remappedTargetId);
                if (target != null && target != Blocks.AIR && target != Items.AIR)
                {
                    missingMapping.remap(target);
                }
            }
        });
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
                itemstack.setTagCompound(new NBTTagCompound());
            }
            final NBTTagCompound compound = itemstack.getTagCompound();

            BlockPosUtil.writeToNBT(compound, FIRST_POS_STRING, event.getPos());
            LanguageHandler.sendPlayerMessage(event.getPlayer(), "item.scepterSteel.point", event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
            itemstack.setTagCompound(compound);

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(@NotNull final TickEvent.WorldTickEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }
        Manager.onWorldTick((WorldServer) event.world);
    }
}
