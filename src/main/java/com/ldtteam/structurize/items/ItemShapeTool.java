package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemShapeTool extends AbstractItemStructurize
{
    /**
     * Sets the name, creative tab, and registers the item.
     * @param properties the properties
     */
    public ItemShapeTool(final Properties properties)
    {
        super("shapeTool", properties.maxStackSize(1));
    }

    @NotNull
    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        if (context.getWorld().isRemote)
        {
            Structurize.proxy.openShapeToolWindow(context.getPos().offset(context.getPlacementHorizontalFacing()));
        }

        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, @NotNull final Hand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);

        if (worldIn.isRemote)
        {
            Structurize.proxy.openShapeToolWindow(null);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }


    @Override
    public ItemStack getContainerItem(final ItemStack itemStack)
    {
        //we want to return the shape tool when use for crafting
        if (ItemStackUtils.isEmpty(itemStack))
        {
            return ItemStackUtils.EMPTY;
        }
        return itemStack.copy();
    }

    @Override
    public boolean hasContainerItem(final ItemStack itemStack)
    {
        //we want to return the shape tool when use for crafting
        return !ItemStackUtils.isEmpty(itemStack);
    }
}
