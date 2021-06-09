package com.ldtteam.structurize.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

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
                if (!stack.isEmpty() && stack.sameItem(content))
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
    public static void transferIntoNextBestSlot(@NotNull final ItemStack stack, @NotNull final IItemHandler targetHandler)
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
        for (int i = 0; i < handler.getSlots(); i++)
        {
            if (handler.getStackInSlot(i).sameItem(tempStack))
            {
                final ItemStack result = handler.extractItem(i, count, false);
                if (result.getCount() == count)
                {
                    return;
                }
                count -= result.getCount();
            }
        }
    }
}
