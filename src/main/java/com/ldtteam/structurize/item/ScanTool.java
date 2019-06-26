package com.ldtteam.structurize.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;

/**
 * ScanTool item class
 */
public class ScanTool extends Item
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
}
