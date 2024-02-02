package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * Structurize specific inventory utilities.
 */
public class InventoryUtils
{
    /**
     * Check if an inventory has all the required stacks.
     * @param inventory the inventory to check.
     * @param requiredItems the list of items.
     * @return true if so, else false.
     */
    public static boolean hasRequiredItems(final IItemHandler inventory, final List<ItemStack> requiredItems)
    {
        final List<ItemStack> listToDiscount = new ArrayList<>();
        for (final ItemStack stack : requiredItems)
        {
            listToDiscount.add(stack.copy());
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++)
        {
            final ItemStack content = inventory.getStackInSlot(slot);
            if (content.isEmpty())
            {
                continue;
            }
            int contentCount = content.getCount();

            for (final ItemStack stack : listToDiscount)
            {
                if (!stack.isEmpty() && ItemStackUtils.compareItemStacksIgnoreStackSize(stack, content))
                {
                    if (stack.getCount() < content.getCount())
                    {
                        contentCount = contentCount - stack.getCount();
                        stack.setCount(0);
                    }
                    else
                    {
                        stack.setCount(stack.getCount() - contentCount);
                        break;
                    }
                }
            }
        }

        for (final ItemStack stack : listToDiscount)
        {
            if (!stack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to transfers a stack to the next best slot in the target inventory.
     *
     * @param targetHandler The {@link IItemHandler} that works as Target.
     */
    public static void transferIntoNextBestSlot(final ItemStack stack, final IItemHandler targetHandler)
    {
        if(stack.isEmpty())
        {
            return;
        }

        ItemStack sourceStack = stack.copy();
        for (int i = 0; i < targetHandler.getSlots(); i++)
        {
            sourceStack = targetHandler.insertItem(i, sourceStack, false);
            if (sourceStack.isEmpty())
            {
                return;
            }
        }
    }

    /**
     * Consume an ItemStack from an itemhandler.
     * @param tempStack the stack.
     * @param handler the handler.
     */
    public static void consumeStack(final ItemStack tempStack, final IItemHandler handler)
    {
        int count = tempStack.getCount();
        final ItemStack container = tempStack.getCraftingRemainingItem();

        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (ItemStackUtils.compareItemStacksIgnoreStackSize(handler.getStackInSlot(i), tempStack))
            {
                final ItemStack result = handler.extractItem(i, count, false);
                if (result.getCount() == count)
                {
                    if (!container.isEmpty())
                    {
                        for (int j = 0; j < tempStack.getCount(); j++)
                        {
                            transferIntoNextBestSlot(container, handler);
                        }
                    }
                    return;
                }
                count -= result.getCount();
            }
        }
    }
}
