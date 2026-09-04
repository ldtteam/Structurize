package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.client.gui.WindowTagTool;
import com.ldtteam.structurize.util.ItemStackNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/**
 * Item for tagging positions with tags
 */
public class ItemTagTool extends AbstractItemWithPosSelector
{
    public static final String TAG_ANCHOR_POS         = "anchorpostag";
    public static final String TAG_CURRENT_TAG        = "currenttag";

    /**
     * Creates default scan tool item.
     */
    public ItemTagTool()
    {
        this(new Item.Properties().durability(0).rarity(Rarity.UNCOMMON));
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
        if (worldIn.isClientSide())
        {
            final BlockPos anchorPos = getAnchorPos(itemStack);
            if (anchorPos == null)
            {
                playerIn.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.noanchor"));
                return InteractionResult.FAIL;
            }

            final WindowTagTool window = new WindowTagTool(getCurrentTag(itemStack), anchorPos, worldIn, itemStack);
            window.open();
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Get the anchor pos from nbt
     *
     * @param stack stack to use
     * @return pos of anchor
     */
    private BlockPos getAnchorPos(final ItemStack stack)
    {
        final CompoundTag itemCompound = ItemStackNbtHelper.getOrCreateCustomTag(stack);

        if (itemCompound.contains(TAG_ANCHOR_POS))
        {
            return BlockPosUtil.readFromNBT(itemCompound, TAG_ANCHOR_POS);
        }

        return null;
    }

    /**
     * Getsthe current tag from nbt
     *
     * @param stack stack to use
     * @return tag string
     */
    private String getCurrentTag(final ItemStack stack)
    {
        if (ItemStackNbtHelper.getOrCreateCustomTag(stack).contains(TAG_CURRENT_TAG))
        {
            return ItemStackNbtHelper.getOrCreateCustomTag(stack).getStringOr(TAG_CURRENT_TAG, "");
        }
        return "";
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        final InteractionResult result = onAirRightClick(
            null,
            null,
            worldIn,
            playerIn,
            playerIn.getItemInHand(handIn));
        return result instanceof final Success success
            ? success.heldItemTransformedTo(playerIn.getItemInHand(handIn))
            : result;
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
                BlockPosUtil.writeToNBT(ItemStackNbtHelper.getOrCreateCustomTag(context.getItemInHand()), TAG_ANCHOR_POS, context.getClickedPos());
                if (context.getLevel().isClientSide())
                {
                    context.getPlayer().sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchorsaved"));
                }
                return InteractionResult.SUCCESS;
            }
            else
            {
                if (context.getLevel().isClientSide())
                {
                    context.getPlayer().sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchor.notvalid"));
                }
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canDestroyBlock(final ItemStack selectedStack,
        final BlockState state,
        final Level worldIn,
        final BlockPos pos,
        final LivingEntity entity)
    {
        if (!(entity instanceof final Player player))
        {
            return super.canDestroyBlock(selectedStack, state, worldIn, pos, entity);
        }

        final ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.tagTool.get())
        {
            return false;
        }

        BlockPos anchorPos = getAnchorPos(stack);
        String currentTag = getCurrentTag(stack);

        if (anchorPos == null)
        {
            player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.noanchor"));
            return false;
        }

        if (currentTag.isEmpty())
        {
            player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.notag"));
            return false;
        }

        // Apply tag to item
        BlockPos relativePos = pos.subtract(anchorPos);

        final BlockEntity te = worldIn.getBlockEntity(anchorPos);
        if (!(te instanceof IBlueprintDataProviderBE))
        {
            player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchor.notvalid"));
            ItemStackNbtHelper.getOrCreateCustomTag(stack).remove(TAG_ANCHOR_POS);
            return false;
        }

        // add/remove tags
        Map<BlockPos, List<String>> tagPosMap = ((IBlueprintDataProviderBE) te).getPositionedTags();

        if (!tagPosMap.containsKey(relativePos) || !tagPosMap.get(relativePos).contains(currentTag))
        {
            ((IBlueprintDataProviderBE) te).addTag(relativePos, currentTag);
            if (worldIn.isClientSide())
            {
                player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.addtag",
                        currentTag,
                        worldIn.getBlockState(pos).getBlock().getName()));
            }
        }
        else
        {
            ((IBlueprintDataProviderBE) te).removeTag(relativePos, currentTag);
            if (worldIn.isClientSide())
            {
                player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.removed",
                        currentTag,
                        worldIn.getBlockState(pos).getBlock().getName()));
            }
        }

        return false;
    }
}
