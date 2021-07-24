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
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.Item.Properties;

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

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            Structurize.proxy.openShapeToolWindow(context.getClickedPos().relative(context.getHorizontalDirection()));
        }

        return InteractionResult.SUCCESS;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, @NotNull final InteractionHand hand)
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
