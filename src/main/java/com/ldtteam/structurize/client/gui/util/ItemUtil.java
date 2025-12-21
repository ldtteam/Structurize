package com.ldtteam.structurize.client.gui.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ItemUtil
{
    /**
     * Creates a list of all items that can be picked
     *
     * @return
     */
    public static List<ItemStack> getAllItems()
    {
        return ImmutableList.copyOf(StreamSupport.stream(Spliterators.spliteratorUnknownSize(BuiltInRegistries.ITEM.iterator(), Spliterator.ORDERED), false)
            .filter(item -> item instanceof AirItem || item instanceof BlockItem || (item instanceof BucketItem
                && ((BucketItem) item).content != Fluids.EMPTY))
            .map(ItemStack::new)
            .collect(Collectors.toList()));
    }
}
