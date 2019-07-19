package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.LanguageHandler;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Caliper Item class. Calculates distances, areas, and volumes.
 */
public class ItemCaliper extends AbstractItemStructurize
{
    private static final RangedAttribute ATTRIBUTE_CALIPER_USE = new RangedAttribute((IAttribute) null, "player.caliperUse", 0.0, 0.0, 1.0);

    private static final double HALF                        = 0.5;
    private static final String ITEM_CALIPER_MESSAGE_SAME   = "item.caliper.message.same";
    private static final String ITEM_CALIPER_MESSAGE_BASE   = "item.caliper.message.base";
    private static final String ITEM_CALIPER_MESSAGE_BY     = "item.caliper.message.by";
    private static final String ITEM_CALIPER_MESSAGE_DIRLEN = "item.caliper.message.directLength";
    private static final String ITEM_CALIPER_MESSAGE_XD     = "item.caliper.message.%sD";

    private BlockPos startPosition;

    /**
     * Caliper constructor. Sets max stack to 1, like other tools.
     */
    public ItemCaliper()
    {
        super("caliper");

        super.setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        maxStackSize = 1;
    }

    @Override
    public EnumActionResult onItemUse(
                                       final PlayerEntity player,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final EnumFacing facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
    {
        // if client world, do nothing
        if (worldIn.isRemote)
        {
            return EnumActionResult.FAIL;
        }

        // if attribute instance is not known, register it.
        IAttributeInstance attribute = player.getEntityAttribute(ATTRIBUTE_CALIPER_USE);
        if (attribute == null)
        {
            attribute = player.getAttributeMap().registerAttribute(ATTRIBUTE_CALIPER_USE);
        }
        // if the value of the attribute is still 0, set the start values. (first point)
        if (attribute.getAttributeValue() < HALF)
        {
            startPosition = pos;
            attribute.setBaseValue(1.0);
            return EnumActionResult.SUCCESS;
        }
        attribute.setBaseValue(0.0);
        //Start == end, same location
        if (startPosition.getX() == pos.getX() && startPosition.getY() == pos.getY() && startPosition.getZ() == pos.getZ())
        {
            LanguageHandler.sendPlayerMessage(player, ITEM_CALIPER_MESSAGE_SAME);
            return EnumActionResult.FAIL;
        }

        return handlePlayerMessage(player, pos);
    }

    private EnumActionResult handlePlayerMessage(@NotNull final PlayerEntity playerIn, @NotNull final BlockPos pos)
    {
        int disX = Math.abs(pos.getX() - startPosition.getX());
        int disY = Math.abs(pos.getY() - startPosition.getY());
        int disZ = Math.abs(pos.getZ() - startPosition.getZ());
        int flag = 3;

        if (startPosition.getX() == pos.getX())
        {
            flag--;
        }
        if (startPosition.getY() == pos.getY())
        {
            flag--;
        }
        if (startPosition.getZ() == pos.getZ())
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

        msg = new StringBuilder(LanguageHandler.format(ITEM_CALIPER_MESSAGE_BASE, msg.toString(), LanguageHandler.format(String.format(ITEM_CALIPER_MESSAGE_XD, flag))));
        if (flag > 1)
        {
            msg.append(LanguageHandler.format(ITEM_CALIPER_MESSAGE_DIRLEN,
                (double) Math.round(1000 * Math.sqrt(Math.pow(disX, 2) + Math.pow(disY, 2) + Math.pow(disZ, 2))) / 1000));
        }

        LanguageHandler.sendPlayerMessage(playerIn, msg.toString());
        return EnumActionResult.SUCCESS;
    }
}