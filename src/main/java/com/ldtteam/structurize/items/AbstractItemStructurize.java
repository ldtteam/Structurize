package com.ldtteam.structurize.items;

import net.minecraft.item.Item;

/**
 * Handles simple things that all items need.
 */
public abstract class AbstractItemStructurize extends Item
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name The name of this item
     */
    public AbstractItemStructurize(final String name, final Properties properties)
    {
        super(properties);
        setRegistryName(name);
    }
}
