package com.ldtteam.structurize.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;

/**
 * ShapeTool item class
 */
public class ShapeTool extends Item
{
    /**
     * Creates default shape tool item.
     *
     * @param itemGroup creative tab
     */
    public ShapeTool(final ItemGroup itemGroup)
    {
        this(new Item.Properties().maxDamage(0).setNoRepair().rarity(Rarity.UNCOMMON).group(itemGroup));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public ShapeTool(final Properties properties)
    {
        super(properties);
        setRegistryName("shapetool");
    }
}
