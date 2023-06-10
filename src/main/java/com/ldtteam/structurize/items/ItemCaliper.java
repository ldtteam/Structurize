package com.ldtteam.structurize.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Caliper Item class. Calculates distances, areas, and volumes.
 */
public class ItemCaliper extends AbstractItemWithPosSelector
{
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

        handlePlayerMessage(start, end, playerIn);

        return InteractionResult.SUCCESS;
    }

    private void handlePlayerMessage(final BlockPos start, final BlockPos end, final Player playerIn)
    {
        int disX = Math.abs(end.getX() - start.getX());
        int disY = Math.abs(end.getY() - start.getY());
        int disZ = Math.abs(end.getZ() - start.getZ());

        List<Integer> distances = new ArrayList<>();
        if (disX != 0)
        {
            distances.add(disX + 1);
        }
        if (disY != 0)
        {
            distances.add(disY + 1);
        }
        if (disZ != 0)
        {
            distances.add(disZ + 1);
        }

        playerIn.displayClientMessage(Component.translatable(String.format(ITEM_CALIPER_MESSAGE_XD, distances.size()),
                distances.toArray(new Object[0])), false);
    }
}
