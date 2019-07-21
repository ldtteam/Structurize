package com.ldtteam.structurize.item;

import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Abstract item mechanic for pos selecting
 */
public abstract class AbstractItemWithPosSelector extends Item
{
    private static final String NBT_START_POS = Utils.createLocationFor("start_pos").toString();
    private static final String NBT_END_POS = Utils.createLocationFor("end_pos").toString();
    private static final String START_POS_TKEY = "structurize.possetter.firstpos";
    private static final String END_POS_TKEY = "structurize.possetter.secondpos";

    /**
     * MC redirect.
     *
     * @param properties item properties
     */
    public AbstractItemWithPosSelector(final Properties properties)
    {
        super(properties);
    }

    /**
     * Is called when player air-right-clicks with item.
     *
     * @param start    first pos
     * @param end      second pos
     * @param worldIn  event world
     * @param playerIn event player
     * @return event result, typically success
     */
    public abstract ActionResultType onAirRightClick(final BlockPos start, final BlockPos end, final World worldIn, final PlayerEntity playerIn);

    /**
     * Uses to search for correct itemstack in both hands.
     *
     * @return item reference from {@link ModItems}
     */
    public abstract AbstractItemWithPosSelector getRegisteredItemInstance();

    /**
     * <p>
     * Structurize: Calls {@link AbstractItemWithPosSelector#onAirRightClick()}.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, final Hand handIn)
    {
        final ItemStack itemstack = playerIn.getHeldItem(handIn);
        final CompoundNBT compound = itemstack.getOrCreateTag();
        return new ActionResult<>(
            onAirRightClick(NBTUtil.readBlockPos(compound.getCompound(NBT_START_POS)), NBTUtil.readBlockPos(compound.getCompound(NBT_END_POS)), worldIn, playerIn),
            itemstack);
    }

    /**
     * <p>
     * Structurize: Captures second position.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final BlockPos pos = context.getPos();
        if (context.getWorld().isRemote())
        {
            LanguageHandler.sendMessageToPlayer(context.getPlayer(), END_POS_TKEY, pos.getX(), pos.getY(), pos.getZ());
        }
        context.getItem().getOrCreateTag().put(NBT_END_POS, NBTUtil.writeBlockPos(pos));
        return ActionResultType.SUCCESS;
    }

    /**
     * <p>
     * Structurize: Prevent block breaking client side. Captures first position client side.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean onBlockStartBreak(final ItemStack itemstack, final BlockPos pos, final PlayerEntity player)
    {
        if (player.getEntityWorld().isRemote())
        {
            itemstack.getOrCreateTag().put(NBT_START_POS, NBTUtil.writeBlockPos(pos));
            LanguageHandler.sendMessageToPlayer(player, START_POS_TKEY, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    /**
     * <p>
     * Structurize: Prevent block breaking server side. Captures first position server side.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean canPlayerBreakBlockWhileHolding(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player)
    {
        if (!player.getEntityWorld().isRemote())
        {
            final ItemStack itemstack;
            if (player.getHeldItemMainhand().getItem().equals(getRegisteredItemInstance()))
            {
                itemstack = player.getHeldItemMainhand();
            }
            else
            {
                itemstack = player.getHeldItemOffhand();
            }
            itemstack.getOrCreateTag().put(NBT_START_POS, NBTUtil.writeBlockPos(pos));
        }
        return false;
    }
}