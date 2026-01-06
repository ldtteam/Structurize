package com.ldtteam.structurize.client.gui.util;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.api.ItemStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Client-side Item utility class
 */
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

    /**
     * Creates a list of all items that can be picked inlcuding player items
     * Client-side
     *
     * @return
     */
    public static List<ItemStack> getAllItemsInlcudingInventory()
    {
        final Set<ItemStorage> items = new HashSet<>();
        for (final Item item : BuiltInRegistries.ITEM)
        {
            if (item instanceof AirItem || item instanceof BlockItem || (item instanceof BucketItem
                && ((BucketItem) item).content != Fluids.EMPTY))
            {
                items.add(new ItemStorage(new ItemStack(item)));
            }
        }

        for (final ItemStack stack : Minecraft.getInstance().player.getInventory().items)
        {
            final Item item = stack.getItem();
            if (item instanceof AirItem || item instanceof BlockItem || (item instanceof BucketItem
                && ((BucketItem) item).content != Fluids.EMPTY))
            {
                items.add(new ItemStorage(stack.copy()));
            }
        }

        final List<ItemStack> stackList = new ArrayList<>(items.size());

        for (final ItemStorage storage : items)
        {
            stackList.add(storage.getItemStack());
        }

        return stackList;
    }
}
