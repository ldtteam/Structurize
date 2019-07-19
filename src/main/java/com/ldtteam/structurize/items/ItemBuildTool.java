package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.creativetab.ModCreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Class handling the buildTool item.
 */
public class ItemBuildTool extends AbstractItemStructurize
{
    /**
     * Instantiates the buildTool on load.
     */
    public ItemBuildTool()
    {
        super("scepterGold");

        super.setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setMaxStackSize(1);
    }

    @NotNull
    @Override
    public EnumActionResult onItemUse(
                                       final PlayerEntity playerIn,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final EnumFacing facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
    {
        //playerIn.addStat(ModAchievements.achievementWandOfbuilding);
        if (worldIn.isRemote)
        {
            Structurize.proxy.openBuildToolWindow(pos.offset(facing));
        }

        return EnumActionResult.SUCCESS;
    }

    @NotNull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, final EnumHand hand)
    {
        final ItemStack stack = playerIn.getHeldItem(hand);

        if (worldIn.isRemote)
        {
            Structurize.proxy.openBuildToolWindow(null);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
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
