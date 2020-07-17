package com.ldtteam.structurize.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public static List<ItemStack> getItemStacksOfTileEntity(final CompoundNBT compound, final World world, final BlockPos pos)
    {
        final List<ItemStack> items = new ArrayList<>();
        final TileEntity tileEntity = TileEntity.readTileEntity(world.getBlockState(pos), compound);
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

    /**
     * Get the list of required resources for entities.
     *
     * @param entity the entity object.
     * @param pos the placer pos..
     * @return a list of stacks.
     */
    public static List<ItemStack> getListOfStackForEntity(final Entity entity, final BlockPos pos)
    {
        if (entity != null)
        {
            final List<ItemStack> request = new ArrayList<>();
            if (entity instanceof ItemFrameEntity)
            {
                final ItemStack stack = ((ItemFrameEntity) entity).getDisplayedItem();
                if (!ItemStackUtils.isEmpty(stack))
                {
                    stack.setCount(1);
                    request.add(stack);
                }
                request.add(new ItemStack(Items.ITEM_FRAME, 1));
            }
            else if (entity instanceof ArmorStandEntity)
            {
                request.add(entity.getPickedResult(new RayTraceResult(Vector3d.copy(pos)) {
                    @NotNull
                    @Override
                    public Type getType()
                    {
                        return Type.ENTITY;
                    }
                }));
                entity.getArmorInventoryList().forEach(request::add);
                entity.getHeldEquipment().forEach(request::add);
            }

            return request.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
