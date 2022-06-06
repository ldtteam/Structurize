package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class ItemShapeTool extends AbstractItemStructurize
{
    /**
     * Sets the name, creative tab, and registers the item.
     * @param properties the properties
     */
    public ItemShapeTool(final Properties properties)
    {
        super("shapetool", properties.stacksTo(1));
    }

    @Override
    @SuppressWarnings("resource")
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            Structurize.proxy.openShapeToolWindow(context.getClickedPos().relative(context.getClickedFace()));
        }

        return InteractionResult.SUCCESS;
    }

        @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand hand)
    {
        final ItemStack stack = playerIn.getItemInHand(hand);

        if (worldIn.isClientSide)
        {
            Structurize.proxy.openShapeToolWindow(null);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }


    @Override
    public ItemStack getContainerItem(final ItemStack itemStack)
    {
        //we want to return the shape tool when use for crafting
        if (ItemStackUtils.isEmpty(itemStack))
        {
            return ItemStack.EMPTY;
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
