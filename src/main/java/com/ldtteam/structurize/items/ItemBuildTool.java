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

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDSTYLE_RELATIVE;
/**
import net.minecraft.world.item.Item.Properties;

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
        super("sceptergold", properties.stacksTo(1));
    }

    @Override
    @SuppressWarnings("resource")
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            Structurize.proxy.openBuildToolWindow(context.getClickedPos().relative(context.getClickedFace()), GROUNDSTYLE_RELATIVE);
        }
        return InteractionResult.SUCCESS;
    }

        @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        final ItemStack stack = playerIn.getItemInHand(handIn);

        if (worldIn.isClientSide)
        {
            Structurize.proxy.openBuildToolWindow(null, GROUNDSTYLE_RELATIVE);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(final ItemStack itemStack)
    {
        //we want to return the build tool when use for crafting
        if (ItemStackUtils.isEmpty(itemStack))
        {
            return ItemStack.EMPTY;
        }
        return itemStack.copy();
    }

    @Override
    public boolean hasCraftingRemainingItem(final ItemStack itemStack)
    {
        //we want to return the build tool when use for crafting
        return !ItemStackUtils.isEmpty(itemStack);
    }
}
