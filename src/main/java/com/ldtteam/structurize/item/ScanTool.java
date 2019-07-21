package com.ldtteam.structurize.item;

import com.ldtteam.structurize.Instances;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ScanTool item class
 */
public class ScanTool extends AbstractItemWithPosSelector
{
    /**
     * Creates default scan tool item.
     *
     * @param itemGroup creative tab
     */
    public ScanTool(final ItemGroup itemGroup)
    {
        this(new Item.Properties().maxDamage(0).setNoRepair().rarity(Rarity.UNCOMMON).group(itemGroup));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public ScanTool(final Properties properties)
    {
        super(properties);
        setRegistryName("scantool");
    }

    @Override
    public ActionResultType onAirRightClick(final BlockPos start, final BlockPos end, final World worldIn, final PlayerEntity playerIn)
    {
        if (!worldIn.isRemote())
        {

        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.SCAN_TOOL;
    }
}
