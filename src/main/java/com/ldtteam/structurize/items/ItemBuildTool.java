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

/**
 * Class handling the buildTool item.
 */
public class ItemBuildTool extends AbstractItemStructurize
{
    /**
     * Instantiates the buildTool on load.
     * @param properties the properties.
     */
    public ItemBuildTool(final Properties properties)
    {
        super("sceptergold", properties.maxStackSize(1));
    }

    @NotNull
    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        if (context.getWorld().isRemote)
        {
            Structurize.proxy.openBuildToolWindow(context.getPos().offset(context.getPlacementHorizontalFacing()));
        }
        return ActionResultType.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, @NotNull final Hand handIn)
    {
        final ItemStack stack = playerIn.getHeldItem(handIn);

        if (worldIn.isRemote)
        {
            Structurize.proxy.openBuildToolWindow(null);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack)
    {
        //we want to return the build tool when use for crafting
        if (ItemStackUtils.isEmpty(itemStack))
        {
            return ItemStackUtils.EMPTY;
        }
        return itemStack.copy();
    }

    @Override
    public boolean hasContainerItem(final ItemStack itemStack)
    {
        //we want to return the build tool when use for crafting
        return !ItemStackUtils.isEmpty(itemStack);
    }
}
