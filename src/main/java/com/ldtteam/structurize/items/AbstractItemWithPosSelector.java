package com.ldtteam.structurize.items;

import com.ldtteam.structurize.util.LanguageHandler;
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

import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;

import net.minecraft.item.Item.Properties;

/**
 * Abstract item mechanic for pos selecting
 */
public abstract class AbstractItemWithPosSelector extends Item
{
    public static final  String NBT_START_POS    = FIRST_POS_STRING;
    public static final  String NBT_END_POS      = SECOND_POS_STRING;
    private static final String START_POS_TKEY   = "item.possetter.firstpos";
    private static final String END_POS_TKEY     = "item.possetter.secondpos";
    private static final String MISSING_POS_TKEY = "item.possetter.missingpos";

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
    public abstract ActionResultType onAirRightClick(BlockPos start, BlockPos end, World worldIn, PlayerEntity playerIn, ItemStack itemStack);

    /**
     * Uses to search for correct itemstack in both hands.
     *
     * @return item reference from {@link ModItems}
     */
    public abstract AbstractItemWithPosSelector getRegisteredItemInstance();

    /**
     * <p>
     * Structurize: Calls {@link AbstractItemWithPosSelector#onAirRightClick(BlockPos, BlockPos, World, PlayerEntity, ItemStack)}.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public ActionResult<ItemStack> use(final World worldIn, final PlayerEntity playerIn, final Hand handIn)
    {
        final ItemStack itemstack = playerIn.getLastHandItem(handIn);
        final CompoundNBT compound = itemstack.getOrCreateTag();

        if (!compound.contains(NBT_START_POS))
        {
            if (worldIn.isClientSide())
            {
                LanguageHandler.sendMessageToPlayer(playerIn, MISSING_POS_TKEY + "1");
            }
            return ActionResult.fail(itemstack);
        }

        if (!compound.contains(NBT_END_POS))
        {
            if (worldIn.isClientSide())
            {
                LanguageHandler.sendMessageToPlayer(playerIn, MISSING_POS_TKEY + "2");
            }
            return ActionResult.fail(itemstack);
        }

        return new ActionResult<>(
            onAirRightClick(
                NBTUtil.readBlockPos(compound.getCompound(NBT_START_POS)),
                NBTUtil.readBlockPos(compound.getCompound(NBT_END_POS)),
                worldIn,
                playerIn,
                itemstack),
            itemstack);
    }

    /**
     * <p>
     * Structurize: Captures second position or Anchor Pos.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public ActionResultType useOn(final ItemUseContext context)
    {
        final BlockPos pos = context.getClickedPos();
        if (context.getLevel().isClientSide())
        {
            LanguageHandler.sendMessageToPlayer(context.getPlayer(), END_POS_TKEY, pos.getX(), pos.getY(), pos.getZ());
        }
        context.getItemInHand().getOrCreateTag().put(NBT_END_POS, NBTUtil.writeBlockPos(pos));
        return ActionResultType.SUCCESS;
    }

    /**
     * <p>
     * Structurize: Prevent block breaking server side.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean canAttackBlock(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player)
    {
        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.getItem().equals(getRegisteredItemInstance()))
        {
            itemstack = player.getOffhandItem();
        }
        itemstack.getOrCreateTag().put(NBT_START_POS, NBTUtil.writeBlockPos(pos));
        if (player.getCommandSenderWorld().isClientSide())
        {
            LanguageHandler.sendMessageToPlayer(player, START_POS_TKEY, pos.getX(), pos.getY(), pos.getZ());
        }
        return false;
    }

    /**
     * Override this so items have instant click in survival.
     */
    @Override
    public float getDestroySpeed(final ItemStack stack, final BlockState state)
    {
        return Float.MAX_VALUE;
    }
}
