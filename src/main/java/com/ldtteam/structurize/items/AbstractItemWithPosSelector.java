package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.ItemStackNbtHelper;
import net.minecraft.network.chat.Component;
import com.ldtteam.structurize.api.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract item mechanic for pos selecting
 */
public abstract class AbstractItemWithPosSelector extends Item
{
    private static final String NBT_START_POS    = FIRST_POS_STRING;
    private static final String NBT_END_POS      = SECOND_POS_STRING;
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
    public abstract InteractionResult onAirRightClick(BlockPos start, BlockPos end, Level worldIn, Player playerIn, ItemStack itemStack);

    /**
     * Uses to search for correct itemstack in both hands.
     *
     * @return item reference from {@link ModItems}
     */
    public abstract AbstractItemWithPosSelector getRegisteredItemInstance();

    /**
     * Structurize: Calls {@link AbstractItemWithPosSelector#onAirRightClick(BlockPos, BlockPos, Level, Player, ItemStack)}.
     * {@inheritDoc}
     */
    @Override
    public InteractionResult use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        final ItemStack itemstack = playerIn.getItemInHand(handIn);
        final CompoundTag compound = ItemStackNbtHelper.getOrCreateCustomTag(itemstack);

        if (!compound.contains(NBT_START_POS))
        {
            if (worldIn.isClientSide())
            {
                playerIn.sendSystemMessage(Component.translatable(MISSING_POS_TKEY + "1"));
            }
            return InteractionResult.FAIL;
        }

        if (!compound.contains(NBT_END_POS))
        {
            if (worldIn.isClientSide())
            {
                playerIn.sendSystemMessage(Component.translatable(MISSING_POS_TKEY + "2"));
            }
            return InteractionResult.FAIL;
        }

        final InteractionResult result =
            onAirRightClick(
                BlockPosUtil.readFromNBT(compound, NBT_START_POS),
                BlockPosUtil.readFromNBT(compound, NBT_END_POS),
                worldIn,
                playerIn,
                itemstack);
        return result instanceof final Success success ? success.heldItemTransformedTo(itemstack) : result;
    }

    /**
     * Structurize: Captures second position or Anchor Pos.
     * {@inheritDoc}
     */
    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        final BlockPos pos = context.getClickedPos();
        if (context.getLevel().isClientSide())
        {
            context.getPlayer().sendSystemMessage(Component.translatable(END_POS_TKEY, pos.getX(), pos.getY(), pos.getZ()));
            Utils.playSuccessSound(context.getPlayer());
        }
        BlockPosUtil.writeToNBT(ItemStackNbtHelper.getOrCreateCustomTag(context.getItemInHand()), NBT_END_POS, pos);
        return InteractionResult.SUCCESS;
    }

    /**
     * Structurize: Prevent block breaking server side.
     * {@inheritDoc}
     */
    @Override
    public boolean canDestroyBlock(final ItemStack selectedStack,
        final BlockState state,
        final Level worldIn,
        final BlockPos pos,
        final LivingEntity entity)
    {
        if (!(entity instanceof final Player player) || !player.isShiftKeyDown())
        {
            return super.canDestroyBlock(selectedStack, state, worldIn, pos, entity);
        }

        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.getItem().equals(getRegisteredItemInstance()))
        {
            itemstack = player.getOffhandItem();
        }
        BlockPosUtil.writeToNBT(ItemStackNbtHelper.getOrCreateCustomTag(itemstack), NBT_START_POS, pos);
        if (player.level().isClientSide())
        {
            Utils.playSuccessSound(player);
            player.sendSystemMessage(Component.translatable(START_POS_TKEY, pos.getX(), pos.getY(), pos.getZ()));
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

    /**
     * Saves the start/end coordinates on this stack.
     * @param tool The tool stack (assumed already been validated)
     * @param start The new start position
     * @param end The new end position
     */
    public static void setBounds(@NotNull final ItemStack tool,
                                 @NotNull final BlockPos start,
                                 @NotNull final BlockPos end)
    {
        final CompoundTag tag = ItemStackNbtHelper.getOrCreateCustomTag(tool);
        BlockPosUtil.writeToNBT(tag, NBT_START_POS, start);
        BlockPosUtil.writeToNBT(tag, NBT_END_POS, end);
    }

    /**
     * Loads the start/end coordinates from this stack.
     * @param tool The tool stack (assumed already been validated)
     * @return the start/end positions
     */
    public static Tuple<BlockPos, BlockPos> getBounds(@NotNull final ItemStack tool)
    {
        final CompoundTag tag = ItemStackNbtHelper.getOrCreateCustomTag(tool);
        final BlockPos start = BlockPosUtil.readFromNBT(tag, NBT_START_POS);
        final BlockPos end = BlockPosUtil.readFromNBT(tag, NBT_END_POS);
        return new Tuple<>(start, end);
    }
}
