package com.ldtteam.structurize.items;

import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.FIRST_POS_STRING;
import static com.ldtteam.structurize.api.util.constant.NbtTagConstants.SECOND_POS_STRING;

import net.minecraft.world.item.Item.Properties;
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
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        final ItemStack itemstack = playerIn.getItemInHand(handIn);
        final CompoundTag compound = itemstack.getOrCreateTag();

        if (!compound.contains(NBT_START_POS))
        {
            if (worldIn.isClientSide())
            {
                LanguageHandler.sendMessageToPlayer(playerIn, MISSING_POS_TKEY + "1");
            }
            return InteractionResultHolder.fail(itemstack);
        }

        if (!compound.contains(NBT_END_POS))
        {
            if (worldIn.isClientSide())
            {
                LanguageHandler.sendMessageToPlayer(playerIn, MISSING_POS_TKEY + "2");
            }
            return InteractionResultHolder.fail(itemstack);
        }

        return new InteractionResultHolder<>(
            onAirRightClick(
                NbtUtils.readBlockPos(compound.getCompound(NBT_START_POS)),
                NbtUtils.readBlockPos(compound.getCompound(NBT_END_POS)),
                worldIn,
                playerIn,
                itemstack),
            itemstack);
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
            LanguageHandler.sendMessageToPlayer(context.getPlayer(), END_POS_TKEY, pos.getX(), pos.getY(), pos.getZ());
        }
        context.getItemInHand().getOrCreateTag().put(NBT_END_POS, NbtUtils.writeBlockPos(pos));
        return InteractionResult.SUCCESS;
    }

    /**
     * Structurize: Prevent block breaking server side.
     * {@inheritDoc}
     */
    @Override
    public boolean canAttackBlock(final BlockState state, final Level worldIn, final BlockPos pos, final Player player)
    {
        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.getItem().equals(getRegisteredItemInstance()))
        {
            itemstack = player.getOffhandItem();
        }
        itemstack.getOrCreateTag().put(NBT_START_POS, NbtUtils.writeBlockPos(pos));
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
        final CompoundTag tag = tool.getOrCreateTag();
        tag.put(NBT_START_POS, NbtUtils.writeBlockPos(start));
        tag.put(NBT_END_POS, NbtUtils.writeBlockPos(end));
    }

    /**
     * Loads the start/end coordinates from this stack.
     * @param tool The tool stack (assumed already been validated)
     * @return the start/end positions
     */
    public static Tuple<BlockPos, BlockPos> getBounds(@NotNull final ItemStack tool)
    {
        final CompoundTag tag = tool.getOrCreateTag();
        final BlockPos start = NbtUtils.readBlockPos(tag.getCompound(NBT_START_POS));
        final BlockPos end = NbtUtils.readBlockPos(tag.getCompound(NBT_END_POS));
        return new Tuple<>(start, end);
    }
}
