package com.ldtteam.structurize.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;

/**
 * BuildTool item class
 */
public class BuildTool extends Item
{
    /**
     * Creates default build tool item.
     *
     * @param itemGroup creative tab
     */
    public BuildTool(final ItemGroup itemGroup)
    {
        this(new Item.Properties().maxDamage(0).setNoRepair().rarity(Rarity.UNCOMMON).group(itemGroup));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public BuildTool(final Properties properties)
    {
        super(properties);
        setRegistryName("buildtool");
    }
}
