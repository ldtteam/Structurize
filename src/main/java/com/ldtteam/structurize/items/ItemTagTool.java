package com.ldtteam.structurize.items;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.client.gui.WindowTagTool;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
        this(new Item.Properties().durability(0).setNoRepair().rarity(Rarity.UNCOMMON));
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
            final BlockPos anchorPos = getAnchorPos(itemStack);
            if (anchorPos == null)
            {
                playerIn.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.noanchor"), false);
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
        final CompoundTag itemCompound = stack.getOrCreateTag();

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
        if (stack.getOrCreateTag().contains(TAG_CURRENT_TAG))
        {
            return stack.getOrCreateTag().getString(TAG_CURRENT_TAG);
        }
        return "";
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
                BlockPosUtil.writeToNBT(context.getItemInHand().getOrCreateTag(), TAG_ANCHOR_POS, context.getClickedPos());
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

        BlockPos anchorPos = getAnchorPos(stack);
        String currentTag = getCurrentTag(stack);

        if (anchorPos == null)
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.noanchor"), false);
            return false;
        }

        if (currentTag.isEmpty())
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.notag"), false);
            return false;
        }

        // Apply tag to item
        BlockPos relativePos = pos.subtract(anchorPos);

        final BlockEntity te = worldIn.getBlockEntity(anchorPos);
        if (!(te instanceof IBlueprintDataProviderBE))
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.anchor.notvalid"), false);
            stack.getOrCreateTag().remove(TAG_ANCHOR_POS);
            return false;
        }

        // add/remove tags
        Map<BlockPos, List<String>> tagPosMap = ((IBlueprintDataProviderBE) te).getPositionedTags();

        if (!tagPosMap.containsKey(relativePos) || !tagPosMap.get(relativePos).contains(currentTag))
        {
            ((IBlueprintDataProviderBE) te).addTag(relativePos, currentTag);
            if (worldIn.isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.addtag",
                        currentTag,
                        worldIn.getBlockState(pos).getBlock().getName()), false);
            }
        }
        else
        {
            ((IBlueprintDataProviderBE) te).removeTag(relativePos, currentTag);
            if (worldIn.isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.tagtool.removed",
                        currentTag,
                        worldIn.getBlockState(pos).getBlock().getName()), false);
            }
        }

        return false;
    }
}
