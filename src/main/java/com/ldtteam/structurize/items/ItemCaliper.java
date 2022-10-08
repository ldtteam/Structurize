package com.ldtteam.structurize.items;

import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.Item.Properties;

/**
 * Caliper Item class. Calculates distances, areas, and volumes.
 */
public class ItemCaliper extends AbstractItemWithPosSelector
{
    private static final String ITEM_CALIPER_MESSAGE_SAME = "item.caliper.message.same";
    private static final String ITEM_CALIPER_MESSAGE_BASE = "item.caliper.message.base";
    private static final String ITEM_CALIPER_MESSAGE_BY = "item.caliper.message.by";
    private static final String ITEM_CALIPER_MESSAGE_XD = "item.caliper.message.%sd";

    /**
     * Caliper constructor. Sets max stack to 1, like other tools.
     * 
     * @param properties
     */
    public ItemCaliper(final Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.caliper.get();
    }

    @Override
    public InteractionResult onAirRightClick(final BlockPos start,
        final BlockPos end,
        final Level worldIn,
        final Player playerIn,
        final ItemStack itemStack)
    {
        if (!worldIn.isClientSide)
        {
            return InteractionResult.FAIL;
        }

        if (start.equals(end))
        {
            playerIn.displayClientMessage(Component.translatable(ITEM_CALIPER_MESSAGE_SAME), false);
            return InteractionResult.FAIL;
        }

        handlePlayerMessage(start, end, playerIn);

        return InteractionResult.SUCCESS;
    }

    private void handlePlayerMessage(final BlockPos start, final BlockPos end, final Player playerIn)
    {
        int disX = Math.abs(end.getX() - start.getX());
        int disY = Math.abs(end.getY() - start.getY());
        int disZ = Math.abs(end.getZ() - start.getZ());
        int flag = 3;

        if (start.getX() == end.getX())
        {
            flag--;
        }
        if (start.getY() == end.getY())
        {
            flag--;
        }
        if (start.getZ() == end.getZ())
        {
            flag--;
        }

        final Component by = Component.empty().append(Component.literal(" "))
                .append(Component.translatable(ITEM_CALIPER_MESSAGE_BY))
                .append(Component.literal(" "));
        MutableComponent msg = Component.empty();
        if (disX != 0)
        {
            disX++;
            msg.append(Component.literal(String.valueOf(disX)));
        }
        if (disY != 0)
        {
            if (!msg.getSiblings().isEmpty()) msg.append(by);
            disY++;
            msg.append(Component.literal(String.valueOf(disY)));
        }
        if (disZ != 0)
        {
            if (!msg.getSiblings().isEmpty()) msg.append(by);
            disZ++;
            msg.append(Component.literal(String.valueOf(disZ)));
        }

        msg = Component.translatable(ITEM_CALIPER_MESSAGE_BASE, msg)
                .append(Component.literal(" "))
                .append(Component.translatable(String.format(ITEM_CALIPER_MESSAGE_XD, flag)));
        playerIn.displayClientMessage(msg, false);
    }
}
