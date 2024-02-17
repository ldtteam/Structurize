package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.ItemStackUtils;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import static com.ldtteam.structurize.api.constants.Constants.GROUNDSTYLE_RELATIVE;
/**
import net.minecraft.world.item.Item.Properties;

 * Class handling the buildTool item.
 */
public class ItemBuildTool extends AbstractItemStructurize
{
    /**
     * Instantiates the buildTool on load.
     */
    public ItemBuildTool()
    {
        super("sceptergold", new Properties().stacksTo(1));
    }

    @Override
    @SuppressWarnings("resource")
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            openBuildToolWindow(context.getClickedPos().relative(context.getClickedFace()), GROUNDSTYLE_RELATIVE);
        }
        return InteractionResult.SUCCESS;
    }

        @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        final ItemStack stack = playerIn.getItemInHand(handIn);

        if (worldIn.isClientSide)
        {
            openBuildToolWindow(null, GROUNDSTYLE_RELATIVE);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private static void openBuildToolWindow(final BlockPos pos, final int groundstyle)
    {
        if (pos == null && RenderingCache.getOrCreateBlueprintPreviewData("blueprint").getBlueprint() == null)
        {
            return;
        }

        if (Minecraft.getInstance().screen != null)
        {
            return;
        }

        new WindowExtendedBuildTool(pos, groundstyle, null, WindowExtendedBuildTool.BLOCK_BLUEPRINT_REQUIREMENT).open();
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
