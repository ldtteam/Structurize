package com.ldtteam.structurize.items;

import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.item.Item.Properties;

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
    public ActionResultType onAirRightClick(final BlockPos start,
        final BlockPos end,
        final World worldIn,
        final PlayerEntity playerIn,
        final ItemStack itemStack)
    {
        if (!worldIn.isClientSide)
        {
            return ActionResultType.FAIL;
        }

        if (start.equals(end))
        {
            LanguageHandler.sendMessageToPlayer(playerIn, ITEM_CALIPER_MESSAGE_SAME);
            return ActionResultType.FAIL;
        }

        handlePlayerMessage(start, end, playerIn);

        return ActionResultType.SUCCESS;
    }

    private void handlePlayerMessage(final BlockPos start, final BlockPos end, final PlayerEntity playerIn)
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

        final String by = " " + LanguageHandler.translateKey(ITEM_CALIPER_MESSAGE_BY) + " ";
        StringBuilder msg = new StringBuilder();
        if (disX != 0)
        {
            disX++;
            msg.append(disX);
            msg.append(by);
        }
        if (disY != 0)
        {
            disY++;
            msg.append(disY);
            msg.append(by);
        }
        if (disZ != 0)
        {
            disZ++;
            msg.append(disZ);
            msg.append(by);
        }
        msg.delete(msg.length() - by.length(), msg.length());

        msg = new StringBuilder(LanguageHandler.translateKeyWithFormat(ITEM_CALIPER_MESSAGE_BASE, msg.toString()));
        msg.append(" ");
        msg.append(LanguageHandler.translateKeyWithFormat(String.format(ITEM_CALIPER_MESSAGE_XD, flag)));
        LanguageHandler.sendMessageToPlayer(playerIn, msg.toString());
    }
}
