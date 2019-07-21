package com.ldtteam.structurize.item;

import java.text.DecimalFormat;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Caliper item class
 */
public class Caliper extends AbstractItemWithPosSelector
{
    private static final String ROOT_TKEY = "structurize.caliper.msg.";
    private static final String MSG_SAME_TKEY = ROOT_TKEY + "same";
    private static final String MSG_BASE_TKEY = ROOT_TKEY + "base";
    private static final String MSG_BY_TKEY = ROOT_TKEY + "by";
    private static final String MSG_DIRLEN_TKEY = ROOT_TKEY + "directLength";
    private static final String MSG_XD_TKEY = ROOT_TKEY + "%sD";

    /**
     * Creates default caliper item.
     *
     * @param itemGroup creative tab
     */
    public Caliper(final ItemGroup itemGroup)
    {
        this(new Item.Properties().maxDamage(0).setNoRepair().group(itemGroup));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public Caliper(final Properties properties)
    {
        super(properties);
        setRegistryName("caliper");
    }

    @Override
    public ActionResultType onAirRightClick(final BlockPos start, final BlockPos end, final World worldIn, final PlayerEntity playerIn)
    {
        if (worldIn.isRemote())
        {
            LanguageHandler.sendMessageToPlayer(playerIn, buildPlayerMessage(start, end), new Object[0]);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.CALIPER;
    }

    /**
     * Builds chat message.
     *
     * @param start pos
     * @param end   pos
     * @return chat message string
     */
    private String buildPlayerMessage(final BlockPos start, final BlockPos end)
    {
        if (start.equals(end))
        {
            return LanguageHandler.translateKey(MSG_SAME_TKEY);
        }

        int disX = Math.abs(start.getX() - end.getX());
        int disY = Math.abs(start.getY() - end.getY());
        int disZ = Math.abs(start.getZ() - end.getZ());
        int flag = 3;
        final String by = " " + LanguageHandler.translateKey(MSG_BY_TKEY) + " ";
        StringBuilder msg = new StringBuilder();
        if (disX != 0)
        {
            disX++;
            msg.append(disX);
            msg.append(by);
        }
        else
        {
            flag--;
        }
        if (disY != 0)
        {
            disY++;
            msg.append(disY);
            msg.append(by);
        }
        else
        {
            flag--;
        }
        if (disZ != 0)
        {
            disZ++;
            msg.append(disZ);
            msg.append(by);
        }
        else
        {
            flag--;
        }
        msg.delete(msg.length() - by.length(), msg.length());
        msg = new StringBuilder(LanguageHandler.translateKeyWithFormat(MSG_BASE_TKEY, msg.toString()));
        msg.append(" ");
        msg.append(LanguageHandler.translateKey(String.format(MSG_XD_TKEY, flag)));
        if (flag > 1)
        {
            msg.append(LanguageHandler.translateKey(MSG_DIRLEN_TKEY));
            msg.append(" ");
            msg.append(new DecimalFormat("#.###").format(Math.sqrt(Math.pow(disX, 2) + Math.pow(disY, 2) + Math.pow(disZ, 2))));
        }
        return msg.toString();
    }

}
