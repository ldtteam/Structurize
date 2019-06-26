package com.ldtteam.structurize.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

/**
 * Caliper item class
 */
public class Caliper extends Item
{
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
}
