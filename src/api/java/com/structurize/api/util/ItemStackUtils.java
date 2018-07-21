package com.structurize.api.util;

import com.structurize.api.compatibility.candb.ChiselAndBitsCheck;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
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
    public static List<ItemStack> getItemStacksOfTileEntity(final NBTTagCompound compound, final World world)
    {
        final List<ItemStack> items = new ArrayList<>();
        final TileEntity tileEntity = TileEntity.create(world, compound);
        if (tileEntity instanceof TileEntityFlowerPot)
        {
            items.add(((TileEntityFlowerPot) tileEntity).getFlowerItemStack());
        }
        else if (tileEntity instanceof TileEntityLockable)
        {
            for (int i = 0; i < ((TileEntityLockable) tileEntity).getSizeInventory(); i++)
            {
                final ItemStack stack = ((TileEntityLockable) tileEntity).getStackInSlot(i);
                if (!ItemStackUtils.isEmpty(stack))
                {
                    items.add(stack);
                }
            }
        }
        else if(tileEntity != null && ChiselAndBitsCheck.isChiselAndBitsTileEntity(tileEntity))
        {
            items.addAll(ChiselAndBitsCheck.getBitStacks(tileEntity));
        }
        else if(tileEntity instanceof TileEntityBed)
        {
            items.add(new ItemStack(Items.BED, 1, ((TileEntityBed) tileEntity).getColor().getMetadata()));
        }
        else if(tileEntity instanceof TileEntityBanner)
        {
            items.add(((TileEntityBanner)tileEntity).getItem());
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

