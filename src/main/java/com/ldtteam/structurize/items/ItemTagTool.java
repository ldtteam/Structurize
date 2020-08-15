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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

/**
 * Item for tagging positions with tags
 */
public class ItemTagTool extends AbstractItemWithPosSelector
{
    public static final String TAG_TOOL_REGISTRY_NAME = "sceptertag";
    public static final String TAG_ANCHOR_POS         = "anchorpostag";
    public static final String TAG_CURRENT_TAG        = "currenttag";

    /**
     * Creates default scan tool item.
     *
     * @param itemGroup creative tab
     */
    public ItemTagTool(final ItemGroup itemGroup)
    {
        this(new Item.Properties().maxDamage(0).setNoRepair().rarity(Rarity.UNCOMMON).group(itemGroup));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public ItemTagTool(final Properties properties)
    {
        super(properties);
        setRegistryName(TAG_TOOL_REGISTRY_NAME);
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.tagTool;
    }

    @Override
    public ActionResultType onAirRightClick(final BlockPos start, final BlockPos end, final World worldIn, final PlayerEntity playerIn, final ItemStack itemStack)
    {
        if (worldIn.isRemote)
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
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        if (context.getPlayer() == null)
        {
            return ActionResultType.SUCCESS;
        }

        if (!context.getWorld().isRemote)
        {
            return ActionResultType.FAIL;
        }

        // Set anchor
        if (context.getPlayer().isSneaking())
        {
            TileEntity te = context.getWorld().getTileEntity(context.getPos());
            if (te instanceof IBlueprintDataProvider)
            {
                BlockPosUtil.writeToNBT(context.getItem().getOrCreateTag(), TAG_ANCHOR_POS, context.getPos());
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
    public boolean canPlayerBreakBlockWhileHolding(final BlockState state, final World worldIn, final BlockPos pos, final PlayerEntity player)
    {
        if (!worldIn.isRemote())
        {
            return false;
        }

        final ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() != ModItems.tagTool || player == null || worldIn == null)
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

        final TileEntity te = worldIn.getTileEntity(anchorPos);
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
              worldIn.getBlockState(pos).getBlock().getTranslationKey()));
        }
        else if (!tagPosMap.get(relativePos).contains(currentTag))
        {
            ((IBlueprintDataProvider) te).addTag(relativePos, currentTag);
            Network.getNetwork().sendToServer(new AddRemoveTagMessage(true, currentTag, relativePos, anchorPos));

            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.addtag", currentTag, new TranslationTextComponent(
              worldIn.getBlockState(pos).getBlock().getTranslationKey()));
        }
        else
        {
            ((IBlueprintDataProvider) te).removeTag(relativePos, currentTag);
            Network.getNetwork().sendToServer(new AddRemoveTagMessage(false, currentTag, relativePos, anchorPos));

            LanguageHandler.sendPlayerMessage(player, "com.ldtteam.structurize.gui.tagtool.removed", currentTag, new TranslationTextComponent(
              worldIn.getBlockState(pos).getBlock().getTranslationKey()));
        }

        return false;
    }
}
