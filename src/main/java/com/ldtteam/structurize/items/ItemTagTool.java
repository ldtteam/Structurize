package com.ldtteam.structurize.items;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.client.gui.WindowTagTool;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Item for tagging positions with tags
 */
public class ItemTagTool extends AbstractItemWithPosSelector
{
    /**
     * Creates default scan tool item.
     */
    public ItemTagTool()
    {
        this(new Properties().durability(0).setNoRepair().rarity(Rarity.UNCOMMON).component(TagData.TYPE, TagData.EMPTY));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public ItemTagTool(final Properties properties)
    {
        super(properties);
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.tagTool.get();
    }

    @Override
    public InteractionResult onAirRightClick(final BlockPos start, final BlockPos end, final Level worldIn, final Player playerIn, final ItemStack itemStack)
    {
        if (worldIn.isClientSide)
        {
            final TagData tagData = itemStack.getOrDefault(TagData.TYPE, TagData.EMPTY);
            if (tagData.anchorPos().isEmpty())
            {
                playerIn.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.noanchor"), false);
                return InteractionResult.FAIL;
            }

            final WindowTagTool window = new WindowTagTool(tagData.currentTag().orElse(""), tagData.anchorPos().get(), worldIn, itemStack);
            window.open();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        return new InteractionResultHolder<>(
          onAirRightClick(
            null,
            null,
            worldIn,
            playerIn,
            playerIn.getItemInHand(handIn)),
          playerIn.getItemInHand(handIn));
    }

    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getPlayer() == null)
        {
            return InteractionResult.SUCCESS;
        }

        // Set anchor
        if (context.getPlayer().isShiftKeyDown())
        {
            BlockEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
            if (te instanceof IBlueprintDataProviderBE)
            {
                context.getItemInHand().update(TagData.TYPE, TagData.EMPTY, tags -> tags.setAnchorPos(context.getClickedPos()));
                if (context.getLevel().isClientSide())
                {
                    context.getPlayer().displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchorsaved"), false);
                }
                return InteractionResult.SUCCESS;
            }
            else
            {
                if (context.getLevel().isClientSide())
                {
                    context.getPlayer().displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchor.notvalid"), false);
                }
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canAttackBlock(final BlockState state, final Level worldIn, final BlockPos pos, final Player player)
    {
        final ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.tagTool.get())
        {
            return false;
        }

        final TagData tagData = stack.getOrDefault(TagData.TYPE, TagData.EMPTY);

        if (tagData.anchorPos().isEmpty())
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.noanchor"), false);
            return false;
        }

        if (tagData.currentTag().isEmpty())
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.notag"), false);
            return false;
        }

        // Apply tag to item
        final BlockPos anchorPos = tagData.anchorPos().get();
        final String currentTag = tagData.currentTag().get();
        BlockPos relativePos = pos.subtract(anchorPos);

        final BlockEntity te = worldIn.getBlockEntity(anchorPos);
        if (!(te instanceof final IBlueprintDataProviderBE blueprintBe))
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchor.notvalid"), false);
            stack.update(TagData.TYPE, TagData.EMPTY, tags -> tags.setAnchorPos(null));
            return false;
        }

        // add/remove tags
        Map<BlockPos, List<String>> tagPosMap = blueprintBe.getPositionedTags();

        if (!tagPosMap.containsKey(relativePos) || !tagPosMap.get(relativePos).contains(currentTag))
        {
            blueprintBe.addTag(relativePos, currentTag);
            if (worldIn.isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.addtag",
                        currentTag,
                        worldIn.getBlockState(pos).getBlock().getName()), false);
            }
        }
        else
        {
            blueprintBe.removeTag(relativePos, currentTag);
            if (worldIn.isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.removed",
                        currentTag,
                        worldIn.getBlockState(pos).getBlock().getName()), false);
            }
        }

        return false;
    }

    /**
     * Data components for storing start and end pos
     */
    public record TagData(Optional<BlockPos> anchorPos, Optional<String> currentTag)
    {
        public static DeferredHolder<DataComponentType<?>, DataComponentType<TagData>> TYPE = null;        
        public static final TagData EMPTY = new TagData(Optional.empty(), Optional.empty());

        public static final Codec<TagData> CODEC = RecordCodecBuilder.create(
            builder -> builder
                .group(BlockPos.CODEC.optionalFieldOf("anchor_pos_tag").forGetter(TagData::anchorPos),
                    Codec.STRING.optionalFieldOf("current_tag").forGetter(TagData::currentTag))
                .apply(builder, TagData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TagData> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
                TagData::anchorPos,
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
                TagData::currentTag,
                TagData::new);

        /**
         * For use with {@link ItemStack#update(DataComponentType, Object, UnaryOperator)}
         */
        public TagData setAnchorPos(final BlockPos pos)
        {
            return new TagData(Optional.ofNullable(pos), currentTag);
        }

        /**
         * For use with {@link ItemStack#update(DataComponentType, Object, UnaryOperator)}
         */
        public TagData setCurrentTag(final String currentTag)
        {
            return new TagData(anchorPos, Optional.ofNullable(currentTag.isEmpty() ? null : currentTag));
        }
    }
}
