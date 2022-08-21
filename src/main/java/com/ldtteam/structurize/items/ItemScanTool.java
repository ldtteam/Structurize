package com.ldtteam.structurize.items;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.client.gui.WindowScan;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.LanguageHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.TranslationConstants.ANCHOR_POS_OUTSIDE_SCHEMATIC;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * Item used to scan structures.
 */
public class ItemScanTool extends AbstractItemWithPosSelector
{
    private static final String ANCHOR_POS_TKEY = "item.possetter.anchorpos";
    private static final String NBT_ANCHOR_POS  = "structurize:anchor_pos";

    /**
     * Creates default scan tool item.
     *
     * @param itemGroup creative tab
     */
    public ItemScanTool(final CreativeModeTab itemGroup)
    {
        this(new Item.Properties().durability(0).setNoRepair().rarity(Rarity.UNCOMMON).tab(itemGroup));
    }

    /**
     * MC constructor.
     *
     * @param properties properties
     */
    public ItemScanTool(final Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult onAirRightClick(final BlockPos start, final BlockPos end, final Level worldIn, final Player playerIn, final ItemStack itemStack)
    {
        Optional<BlockPos> anchorPos = Optional.empty();
        if (itemStack.getOrCreateTag().contains(NBT_ANCHOR_POS))
        {
            final BlockPos anchorBlockPos = NbtUtils.readBlockPos(itemStack.getOrCreateTag().getCompound(NBT_ANCHOR_POS));

            if (BlockPosUtil.isInbetween(anchorBlockPos, start, end))
            {
                anchorPos = Optional.of(anchorBlockPos);
                if (worldIn.isClientSide && RenderingCache.getBoxPreviewData("scan") != null)
                {
                    RenderingCache.getBoxPreviewData("scan").setAnchor(anchorPos);
                }
            }
            else
            {
                if (worldIn.isClientSide)
                {
                    playerIn.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.scantool.outsideanchor"), playerIn.getUUID());
                }
            }
        }

        if (!worldIn.isClientSide)
        {
            if (playerIn.isShiftKeyDown())
            {
                saveStructure(worldIn, start, end, playerIn, null, true, anchorPos);
            }
        }
        else
        {
            if (!playerIn.isShiftKeyDown())
            {
                final WindowScan window = new WindowScan(start, end, anchorPos);
                window.open();
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public AbstractItemWithPosSelector getRegisteredItemInstance()
    {
        return ModItems.scanTool.get();
    }

    /**
     * Scan the structure and save it to the disk.
     *
     * @param world  Current world.
     * @param from   First corner.
     * @param to     Second corner.
     * @param player causing this action.
     * @param name   the name of it.
     */
    public static void saveStructure(
      final Level world,
      final BlockPos from,
      final BlockPos to,
      final Player player,
      final String name)
    {
        saveStructure(world, from, to, player, name, true, Optional.empty());
    }

    /**
     * Scan the structure and save it to the disk.
     *
     * @param world        Current world.
     * @param from         First corner.
     * @param to           Second corner.
     * @param player       causing this action.
     * @param name         the name of it.
     * @param saveEntities whether to scan in entities
     */
    public static void saveStructure(
      final Level world,
      final BlockPos from,
      final BlockPos to,
      final Player player,
      final String name,
      final boolean saveEntities,
      final Optional<BlockPos> anchorPos)
    {
        if (anchorPos.isPresent())
        {
            if (!BlockPosUtil.isInbetween(anchorPos.get(), from, to))
            {
                LanguageHandler.sendPlayerMessage(player, ANCHOR_POS_OUTSIDE_SCHEMATIC);
                return;
            }
        }

        final BlockPos blockpos =
          new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        final BlockPos blockpos1 =
          new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        final BlockPos size = blockpos1.subtract(blockpos).offset(1, 1, 1);
        if (size.getX() * size.getY() * size.getZ() > Structurize.getConfig().getServer().schematicBlockLimit.get())
        {
            LanguageHandler.sendPlayerMessage(player, MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get());
            return;
        }

        final long currentMillis = System.currentTimeMillis();
        final String currentMillisString = Long.toString(currentMillis);
        String fileName;
        if (name == null || name.isEmpty())
        {
            fileName = new TranslatableComponent("item.sceptersteel.scanformat", "", currentMillisString).getString();
        }
        else
        {
            fileName = name;
        }

        if (!fileName.contains(".blueprint"))
        {
            fileName+= ".blueprint";
        }

        final Blueprint bp = BlueprintUtil.createBlueprint((ServerLevel) world, blockpos, saveEntities, (short) size.getX(), (short) size.getY(), (short) size.getZ(), fileName, anchorPos);

        if (!anchorPos.isPresent() && bp.getPrimaryBlockOffset().equals(new BlockPos(bp.getSizeX() / 2, 0, bp.getSizeZ() / 2)))
        {
            final List<BlockInfo> list = bp.getBlockInfoAsList().stream()
              .filter(blockInfo -> blockInfo.hasTileEntityData() && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
              .collect(Collectors.toList());

            if (list.size() > 1)
            {
                player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.scantool.scanbadanchor", fileName), player.getUUID());
            }
        }

        Network.getNetwork().sendToPlayer(new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(bp), fileName), (ServerPlayer) player);
    }

    @Override
    public boolean canAttackBlock(final BlockState state, final Level worldIn, final BlockPos pos, final Player player)
    {
        if (!player.isShiftKeyDown())
        {
            return super.canAttackBlock(state, worldIn, pos, player);
        }

        if (worldIn.isClientSide())
        {
            LanguageHandler.sendMessageToPlayer(player, ANCHOR_POS_TKEY, pos.getX(), pos.getY(), pos.getZ());
        }

        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.getItem().equals(getRegisteredItemInstance()))
        {
            itemstack = player.getOffhandItem();
        }

        final BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof IBlueprintDataProviderBE && !((IBlueprintDataProviderBE) te).getSchematicName().isEmpty())
        {
            if (worldIn.isClientSide && RenderingCache.getBoxPreviewData("scan") != null)
            {
                RenderingCache.getBoxPreviewData("scan").setAnchor(Optional.of(pos));
            }

            final BlockPos start = ((IBlueprintDataProviderBE) te).getInWorldCorners().getA();
            final BlockPos end = ((IBlueprintDataProviderBE) te).getInWorldCorners().getB();

            if (!(start.equals(pos)) && !(end.equals(pos)))
            {
                if (worldIn.isClientSide)
                {
                    RenderingCache.queue("scan", new BoxPreviewData(((IBlueprintDataProviderBE) te).getInWorldCorners().getA(), ((IBlueprintDataProviderBE) te).getInWorldCorners().getB(), Optional.of(pos)));
                }
                itemstack.getOrCreateTag().put(NBT_START_POS, NbtUtils.writeBlockPos(start));
                itemstack.getOrCreateTag().put(NBT_END_POS, NbtUtils.writeBlockPos(end));
            }
        }

        itemstack.getOrCreateTag().put(NBT_ANCHOR_POS, NbtUtils.writeBlockPos(pos));
        return false;
    }
}
