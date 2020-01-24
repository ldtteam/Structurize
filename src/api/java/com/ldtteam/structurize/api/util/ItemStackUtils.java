package com.ldtteam.structurize.api.util;

import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for the inventories.
 */
public final class ItemStackUtils
{
    /**
     * Variable representing the empty itemstack in 1.10.
     * Used for easy updating to 1.11
     */
    public static final ItemStack EMPTY = ItemStack.EMPTY;

    /**
     * Private constructor to hide the implicit one.
     */
    private ItemStackUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Get itemStack of tileEntityData. Retrieve the data from the tileEntity.
     *
     * @param compound the tileEntity stored in a compound.
     * @param world the world.
     * @return the list of itemstacks.
     */
    public static List<ItemStack> getItemStacksOfTileEntity(final CompoundNBT compound, final World world)
    {
        final List<ItemStack> items = new ArrayList<>();
        final TileEntity tileEntity = TileEntity.create(compound);
        if (tileEntity instanceof LockableTileEntity)
        {
            for (int i = 0; i < ((LockableTileEntity) tileEntity).getSizeInventory(); i++)
            {
                final ItemStack stack = ((LockableTileEntity) tileEntity).getStackInSlot(i);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    items.add(stack);
                }
            }
        }
        return items;
    }

    /**
     * Wrapper method to check if a stack is empty.
     * Used for easy updating to 1.11.
     *
     * @param stack The stack to check.
     * @return True when the stack is empty, false when not.
     */
    @NotNull
    public static Boolean isEmpty(@Nullable final ItemStack stack)
    {
        return stack == null || stack == EMPTY || stack.getCount() <= 0;
    }

    /**
     * get the size of the stack.
     * This is for compatibility between 1.10 and 1.11
     *
     * @param stack to get the size from
     * @return the size of the stack
     */
    public static int getSize(final ItemStack stack)
    {
        if (ItemStackUtils.isEmpty(stack))
        {
            return 0;
        }

        return stack.getCount();
    }
}

