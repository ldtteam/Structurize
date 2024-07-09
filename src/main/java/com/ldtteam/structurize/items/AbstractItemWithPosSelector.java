package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract item mechanic for pos selecting
 */
public abstract class AbstractItemWithPosSelector extends Item
{
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
        super(properties.component(PosSelection.TYPE, PosSelection.EMPTY));
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
        final PosSelection compound = itemstack.getOrDefault(PosSelection.TYPE, PosSelection.EMPTY);

        if (compound.startPos().isEmpty())
        {
            if (worldIn.isClientSide())
            {
                playerIn.displayClientMessage(Component.translatable(MISSING_POS_TKEY + "1"), false);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        if (compound.endPos().isEmpty())
        {
            if (worldIn.isClientSide())
            {
                playerIn.displayClientMessage(Component.translatable(MISSING_POS_TKEY + "2"), false);
            }
            return InteractionResultHolder.fail(itemstack);
        }

        return new InteractionResultHolder<>(
            onAirRightClick(
                compound.startPos().get(),
                compound.endPos().get(),
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
            context.getPlayer().displayClientMessage(Component.translatable(END_POS_TKEY, pos.getX(), pos.getY(), pos.getZ()), false);
            Utils.playSuccessSound(context.getPlayer());
        }
        context.getItemInHand().update(PosSelection.TYPE, PosSelection.EMPTY, data -> data.setEndpos(pos));
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
        itemstack.update(PosSelection.TYPE, PosSelection.EMPTY, data -> data.setStartPos(pos));
        if (player.getCommandSenderWorld().isClientSide())
        {
            Utils.playSuccessSound(player);
            player.displayClientMessage(Component.translatable(START_POS_TKEY, pos.getX(), pos.getY(), pos.getZ()), false);
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
     * @deprecated use datacomponents
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static void setBounds(@NotNull final ItemStack tool,
                                 @NotNull final BlockPos start,
                                 @NotNull final BlockPos end)
    {
        tool.update(PosSelection.TYPE, PosSelection.EMPTY, data -> data.setSelection(start, end));
    }

    /**
     * Loads the start/end coordinates from this stack.
     * @param tool The tool stack (assumed already been validated)
     * @return the start/end positions
     * @deprecated use datacomponents
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static Tuple<BlockPos, BlockPos> getBounds(@NotNull final ItemStack tool)
    {
        final PosSelection tag = tool.getOrDefault(PosSelection.TYPE, PosSelection.EMPTY);
        return new Tuple<>(tag.startPos().orElse(null), tag.endPos().orElse(null));
    }

    /**
     * Data components for storing start and end pos
     */
    public record PosSelection(Optional<BlockPos> startPos, Optional<BlockPos> endPos)
    {
        public static DeferredHolder<DataComponentType<?>, DataComponentType<PosSelection>> TYPE = null;        
        public static final PosSelection EMPTY = new PosSelection(Optional.empty(), Optional.empty());

        public static final Codec<PosSelection> CODEC = RecordCodecBuilder.create(
            builder -> builder
                .group(BlockPos.CODEC.optionalFieldOf("start_pos").forGetter(PosSelection::startPos),
                    BlockPos.CODEC.optionalFieldOf("end_pos").forGetter(PosSelection::endPos))
                .apply(builder, PosSelection::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PosSelection> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
                PosSelection::startPos,
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
                PosSelection::endPos,
                PosSelection::new);

        /**
         * @return true if both start and end positions are set
         */
        public boolean hasSelection()
        {
            return startPos.isPresent() && endPos.isPresent();
        }

        /**
         * For use with {@link ItemStack#update(DataComponentType, Object, UnaryOperator)}
         */
        public PosSelection setStartPos(final BlockPos pos)
        {
            return new PosSelection(Optional.ofNullable(pos), endPos);
        }

        /**
         * For use with {@link ItemStack#update(DataComponentType, Object, UnaryOperator)}
         */
        public PosSelection setEndpos(final BlockPos pos)
        {
            return new PosSelection(startPos, Optional.ofNullable(pos));
        }

        /**
         * For use with {@link ItemStack#update(DataComponentType, Object, UnaryOperator)}
         */
        public PosSelection setSelection(final BlockPos startPos, final BlockPos endPos)
        {
            return new PosSelection(Optional.ofNullable(startPos), Optional.ofNullable(endPos));
        }
    }
}
