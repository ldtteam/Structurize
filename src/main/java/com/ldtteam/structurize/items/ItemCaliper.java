package com.ldtteam.structurize.items;

import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
        super(properties.maxStackSize(1));
        setRegistryName("caliper");
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.caliper;
    }

    @Override
    public ActionResultType onAirRightClick(final BlockPos start,
        final BlockPos end,
        final World worldIn,
        final PlayerEntity playerIn,
        final ItemStack itemStack)
    {
        if (start.equals(end))
        {
            LanguageHandler.sendPlayerMessage(playerIn, ITEM_CALIPER_MESSAGE_SAME);
            return ActionResultType.FAIL;
        }

        return handlePlayerMessage(start, end, playerIn);
    }

    private ActionResultType handlePlayerMessage(final BlockPos start, final BlockPos end, final PlayerEntity playerIn)
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

        final String by = " " + LanguageHandler.format(ITEM_CALIPER_MESSAGE_BY) + " ";
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

        msg = new StringBuilder(LanguageHandler.format(ITEM_CALIPER_MESSAGE_BASE, msg.toString()));
        msg.append(" ");
        msg.append(LanguageHandler.format(String.format(ITEM_CALIPER_MESSAGE_XD, flag)));
        LanguageHandler.sendPlayerMessage(playerIn, msg.toString());
        return ActionResultType.SUCCESS;
    }
}
