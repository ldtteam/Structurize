package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.client.gui.WindowTagTool;
import com.ldtteam.structurize.network.messages.AddRemoveTagMessage;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

import net.minecraft.item.Item.Properties;

/**
 * Item for tagging positions with tags
 */
public class ItemTagTool extends AbstractItemWithPosSelector
{
    public static final String TAG_ANCHOR_POS         = "anchorpostag";
    public static final String TAG_CURRENT_TAG        = "currenttag";

    /**
     * Creates default scan tool item.
     *
     * @param itemGroup creative tab
     */
    public ItemTagTool(final ItemGroup itemGroup)
    {
        this(new Item.Properties().durability(0).setNoRepair().rarity(Rarity.UNCOMMON).tab(itemGroup));
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
    public ActionResultType onAirRightClick(final BlockPos start, final BlockPos end, final World worldIn, final PlayerEntity playerIn, final ItemStack itemStack)
    {
        if (worldIn.isClientSide)
        {
            final BlockPos anchorPos = getAnchorPos(itemStack);
            if (anchorPos == null)
            {
                LanguageHandler.sendPlayerMessage(playerIn, "com.ldtteam.structurize.gui.tagtool.noanchor");
                return ActionResultType.FAIL;
            }

            final WindowTagTool window = new WindowTagTool(getCurrentTag(itemStack), anchorPos, worldIn, itemStack);
            window.open();
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * Get the anchor pos from nbt
     *
     * @param stack stack to use
     * @return pos of anchor
     */
    private BlockPos getAnchorPos(final ItemStack stack)
    {
        final CompoundNBT itemCompound = stack.getOrCreateTag();

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
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        return new ActionResult<>(
          onAirRightClick(
            null,
            null,
            worldIn,
            playerIn,
            playerIn.getItemInHand(handIn)),
          playerIn.getItemInHand(handIn));
    }

    @Override
    public ActionResultType useOn(final ItemUseContext context)
    {
        if (context.getPlayer() == null)
        {
            return ActionResultType.SUCCESS;
        }

        if (!context.getLevel().isClientSide)
        {
            return ActionResultType.FAIL;
        }

        // Set anchor
        if (context.getPlayer().isShiftKeyDown())
        {
            TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
            if (te instanceof IBlueprintDataProvider)
            {
                BlockPosUtil.writeToNBT(context.getItemInHand().getOrCreateTag(), TAG_ANCHOR_POS, context.getClickedPos());
                LanguageHandler.sendPlayerMessage(context.getPlayer(), "com.ldtteam.structurize.gui.tagtool.anchorsaved");
                return ActionResultType.SUCCESS;
            }
            else
            {
                LanguageHandler.sendPlayerMessage(context.getPlayer(), "com.ldtteam.structurize.gui.tagtool.anchor.notvalid");
                return ActionResultType.FAIL;
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean canAttackBlock(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player)
    {
        if (!worldIn.isClientSide())
        {
            return false;
        }

        final ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.tagTool.get() || player == null || worldIn == null)
        {
            return false;
        }

        BlockPos anchorPos = getAnchorPos(stack);
        String currentTag = getCurrentTag(stack);

        if (anchorPos == null)
        {
            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.noanchor");
            return false;
        }

        if (currentTag.isEmpty())
        {
            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.notag");
            return false;
        }

        // Apply tag to item
        BlockPos relativePos = pos.subtract(anchorPos);

        final TileEntity te = worldIn.getBlockEntity(anchorPos);
        if (!(te instanceof IBlueprintDataProvider))
        {
            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.anchor.notvalid");
            stack.getOrCreateTag().remove(TAG_ANCHOR_POS);
            return false;
        }

        // add/remove tags
        Map<BlockPos, List<String>> tagPosMap = ((IBlueprintDataProvider) te).getPositionedTags();

        if (!tagPosMap.containsKey(relativePos))
        {
            ((IBlueprintDataProvider) te).addTag(relativePos, currentTag);
            Network.getNetwork().sendToServer(new AddRemoveTagMessage(true, currentTag, relativePos, anchorPos));

            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.addtag", currentTag, new TranslationTextComponent(
              worldIn.getBlockState(pos).getBlock().getDescriptionId()));
        }
        else if (!tagPosMap.get(relativePos).contains(currentTag))
        {
            ((IBlueprintDataProvider) te).addTag(relativePos, currentTag);
            Network.getNetwork().sendToServer(new AddRemoveTagMessage(true, currentTag, relativePos, anchorPos));

            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.addtag", currentTag, new TranslationTextComponent(
              worldIn.getBlockState(pos).getBlock().getDescriptionId()));
        }
        else
        {
            ((IBlueprintDataProvider) te).removeTag(relativePos, currentTag);
            Network.getNetwork().sendToServer(new AddRemoveTagMessage(false, currentTag, relativePos, anchorPos));

            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.removed", currentTag, new TranslationTextComponent(
              worldIn.getBlockState(pos).getBlock().getDescriptionId()));
        }

        return false;
    }
}
