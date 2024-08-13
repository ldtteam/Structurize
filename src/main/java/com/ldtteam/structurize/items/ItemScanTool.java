package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.BlockPosUtil;
import com.ldtteam.structurize.api.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.IScrollableItem;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.client.gui.WindowScan;
import com.ldtteam.structurize.commands.ScanCommand;
import com.ldtteam.structurize.component.ModDataComponents;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import com.ldtteam.structurize.network.messages.ShowScanMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.ScanToolData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.constants.Constants.MOD_ID;
import static com.ldtteam.structurize.api.constants.TranslationConstants.ANCHOR_POS_OUTSIDE_SCHEMATIC;
import static com.ldtteam.structurize.api.constants.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * Item used to scan structures.
 */
public class ItemScanTool extends AbstractItemWithPosSelector implements IScrollableItem, ISpecialBlockPickItem
{
    private static final String ANCHOR_POS_TKEY = "item.possetter.anchorpos";

    /**
     * Creates default scan tool item.
     */
    public ItemScanTool()
    {
        this(new Properties().durability(0)
            .setNoRepair()
            .rarity(Rarity.UNCOMMON)
            .component(ModDataComponents.SCAN_TOOL, ScanToolData.EMPTY));
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
        final ScanToolData data = ScanToolData.updateItemStack(itemStack, d -> saveSlot(d, itemStack, playerIn));

        if (!worldIn.isClientSide)
        {
            if (playerIn.isShiftKeyDown())
            {
                saveStructure(worldIn, playerIn, data.currentSlot(), true);
            }
        }
        else
        {
            if (!playerIn.isShiftKeyDown())
            {
                final WindowScan window = new WindowScan(data);
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
     * @param world        Current world.
     * @param player       causing this action.
     * @param slot         the scan data.
     * @param saveEntities whether to scan in entities
     */
    public static void saveStructure(
      final Level world,
      final Player player,
      final ScanToolData.Slot slot,
      final boolean saveEntities)
    {
        if (slot.box().anchor().isPresent())
        {
            if (!BlockPosUtil.isInbetween(slot.box().anchor().get(), slot.box().pos1(), slot.box().pos2()))
            {
                player.displayClientMessage(Component.translatable(ANCHOR_POS_OUTSIDE_SCHEMATIC), false);
                return;
            }
        }

        final BoundingBox box = BoundingBox.fromCorners(slot.box().pos1(), slot.box().pos2());
        if (box.getXSpan() * box.getYSpan() * box.getZSpan() > Structurize.getConfig().getServer().schematicBlockLimit.get())
        {
            player.displayClientMessage(Component.translatable(MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get()), false);
            return;
        }

        String fileName;
        if (slot.name().isEmpty())
        {
            fileName = Component.translatable("item.sceptersteel.scanformat", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date.from(Instant.now()))).getString();
        }
        else
        {
            fileName = slot.name();
        }

        if (!fileName.contains(".blueprint"))
        {
            fileName+= ".blueprint";
        }

        final BlockPos zero = new BlockPos(box.minX(), box.minY(), box.minZ());
        final Blueprint bp = BlueprintUtil.createBlueprint(world, zero, saveEntities, (short) box.getXSpan(), (short) box.getYSpan(), (short) box.getZSpan(), fileName, slot.box().anchor());

        if (slot.box().anchor().isEmpty() && bp.getPrimaryBlockOffset().equals(new BlockPos(bp.getSizeX() / 2, 0, bp.getSizeZ() / 2)))
        {
            final List<BlockInfo> list = bp.getBlockInfoAsList().stream()
              .filter(blockInfo -> blockInfo.hasTileEntityData() && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
              .collect(Collectors.toList());

            if (list.size() > 1)
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.scanbadanchor", fileName), false);
            }
        }

        new SaveScanMessage(BlueprintUtil.writeBlueprintToNBT(bp), fileName).sendToPlayer((ServerPlayer) player);
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
            player.displayClientMessage(Component.translatable(ANCHOR_POS_TKEY, pos.getX(), pos.getY(), pos.getZ()), false);
        }

        ItemStack itemstack = player.getMainHandItem();
        if (!itemstack.getItem().equals(getRegisteredItemInstance()))
        {
            itemstack = player.getOffhandItem();
        }

        final BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof final IBlueprintDataProviderBE bpProvider && !bpProvider.getSchematicName().isEmpty())
        {
            final BlockPos start = bpProvider.getInWorldCorners().getA();
            final BlockPos end = bpProvider.getInWorldCorners().getB();

            if (!(start.equals(pos)) && !(end.equals(pos)))
            {
                if (worldIn.isClientSide)
                {
                    RenderingCache.queue("scan", new BoxPreviewData(bpProvider.getInWorldCorners().getA(), bpProvider.getInWorldCorners().getB(), Optional.of(pos)));
                }
                PosSelection.updateItemStack(itemstack, data -> data.setSelection(start, end));
            }
            else
            {
                if (worldIn.isClientSide && RenderingCache.getBoxPreviewData("scan") != null)
                {
                    RenderingCache.queue("scan", RenderingCache.getBoxPreviewData("scan").withAnchor(Optional.of(pos)));
                }
            }
        }

        setAnchorPos(itemstack, pos);
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @Nullable TooltipContext world,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flags)
    {
        super.appendHoverText(stack, world, tooltip, flags);

        if (stack.has(ModDataComponents.SCAN_TOOL))
        {
            tooltip.add(getCurrentSlotDescription(stack));
        }
    }

    @Override
    public Component getHighlightTip(@NotNull final ItemStack stack, @NotNull final Component displayName)
    {
        return Component.empty()
                .append(super.getHighlightTip(stack, displayName))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(getCurrentSlotDescription(stack));
    }

    private Component getCurrentSlotDescription(@NotNull final ItemStack stack)
    {
        final ScanToolData data = ScanToolData.readFromItemStack(stack);
        MutableComponent desc = Component.empty()
                .append(Component.literal(Integer.toString(data.currentSlotId())).withStyle(ChatFormatting.GRAY));

        final String name = data.currentSlot().name();
        if (!name.isEmpty())
        {
            desc = desc.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(name));
        }

        return desc;
    }

    @NotNull
    @Override
    public InteractionResult onBlockPick(@NotNull final Player player,
                                         @NotNull final ItemStack stack,
                                         @Nullable final BlockPos pos,
                                         final boolean ctrlKey)
    {
        if (pos == null)
        {
            // treat pick in air like mouse scrolling (just in case someone doesn't have a wheel)
            final double delta = player.isShiftKeyDown() ? -1 : 1;
            return onMouseScroll(player, stack, 0, delta, ctrlKey);
        }

        if (player.level().getBlockEntity(pos) instanceof CommandBlockEntity command)
        {
            return onCommandBlockPick(player, stack, command, ctrlKey);
        }

        // otherwise do standard pick-block
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResult onMouseScroll(@NotNull final Player player,
                                           @NotNull final ItemStack stack,
                                           final double deltaX,
                                           final double deltaY,
                                           final boolean ctrlKey)
    {
        if (player.level().isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        switchSlot((ServerPlayer) player, stack, deltaY < 0 ? ScanToolData::prevSlot : ScanToolData::nextSlot);

        return InteractionResult.SUCCESS;
    }

    private void switchSlot(@NotNull final ServerPlayer player,
                            @NotNull final ItemStack stack,
                            @NotNull final UnaryOperator<ScanToolData> action)
    {
        final ScanToolData data = ScanToolData.updateItemStack(stack, d -> action.apply(saveSlot(d, stack, player)));
        final ScanToolData.Slot slot = loadSlot(data, stack);

        new ShowScanMessage(slot.box()).sendToPlayer(player);
    }

    private ScanToolData saveSlot(@NotNull final ScanToolData data,
                                  @NotNull final ItemStack stack,
                                  @NotNull final Player player)
    {
        final BoxPreviewData box = getBox(stack, player);
        return data.withCurrentSlot(box == null ? null : data.currentSlot().withBox(box));
    }

    public ScanToolData.Slot loadSlot(@NotNull final ScanToolData data,
                                      @NotNull final ItemStack stack)
    {
        final ScanToolData.Slot slot = data.currentSlot();

        // this seems a little silly at first, duplicating this info outside the slot storage.
        // but it preserves compatibility with AbstractItemWithPosSelector.
        PosSelection.updateItemStack(stack, data1 -> data1.setSelection(slot.box().pos1(), slot.box().pos2()));

        return slot;
    }

    /**
     * Called on both client and server side when [shift-]pick-blocking a Command Block.
     * @param player the player
     * @param stack the scan tool
     * @param command the command block entity
     * @param ctrlKey ctrl key is held
     * @return PASS to do the normal action, SUCCESS to pass to server, FAILURE to stop
     */
    private InteractionResult onCommandBlockPick(@NotNull final Player player,
                                                 @NotNull final ItemStack stack,
                                                 @NotNull final CommandBlockEntity command,
                                                 final boolean ctrlKey)
    {
        if (!player.isCreative())
        {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown())
        {
            onCommandBlockPaste((ServerPlayer) player, stack, command, ctrlKey);
        }
        else
        {
            onCommandBlockCopy((ServerPlayer) player, stack, command, ctrlKey);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Called on server side when middle-clicking a command block.
     * @param player the player
     * @param stack the scan tool
     * @param command the command block
     * @param ctrlKey ctrl key is held
     */
    private void onCommandBlockCopy(@NotNull final ServerPlayer player,
                                    @NotNull final ItemStack stack,
                                    @NotNull final CommandBlockEntity command,
                                    final boolean ctrlKey)
    {
        final StringReader reader = new StringReader(command.getCommandBlock().getCommand());
        if (reader.canRead() && reader.peek() == '/') { reader.read(); }

        final CommandDispatcher<CommandSourceStack> dispatcher = player.level().getServer().getCommands().getDispatcher();
        final ParseResults<CommandSourceStack> parsed = dispatcher.parse(reader, command.getCommandBlock().createCommandSourceStack());
        if (parsed.getReader().canRead() || parsed.getContext().getNodes().size() < 4
                || !parsed.getContext().getNodes().get(0).getNode().getName().equals(MOD_ID)
                || !parsed.getContext().getNodes().get(1).getNode().getName().equals(ScanCommand.NAME))
        {
            player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.copy.notscan"));
            return;
        }

        final CommandContext<CommandSourceStack> cmdContext = parsed.getContext().build(parsed.getReader().getString());
        try
        {
            final BlockPos from = BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.POS1);
            final BlockPos to = BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.POS2);
            final Optional<BlockPos> anchor;
            if (parsed.getContext().getArguments().containsKey(ScanCommand.ANCHOR_POS))
            {
                anchor = Optional.of(BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.ANCHOR_POS));
            }
            else
            {
                anchor = Optional.empty();
            }
            final String name;
            if (parsed.getContext().getArguments().containsKey(ScanCommand.FILE_NAME))
            {
                name = StringArgumentType.getString(cmdContext, ScanCommand.FILE_NAME);
            }
            else
            {
                name = "";
            }

            final ScanToolData data = ScanToolData.updateItemStack(stack, d -> d.withCommandBlock(command)
                    .withCurrentSlot(new ScanToolData.Slot(name, new BoxPreviewData(from, to, anchor))));
            final ScanToolData.Slot slot = loadSlot(data, stack);
            new ShowScanMessage(slot.box()).sendToPlayer(player);

            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.copy.ok", name), false);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        catch (CommandSyntaxException e)
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.copy.notscan"), false);
        }
    }

    /**
     * Called on server side when shift-middle-clicking a command block.
     * @param player the player
     * @param stack the scan tool
     * @param command the command block
     * @param ctrlKey ctrl key is held
     */
    private void onCommandBlockPaste(@NotNull final ServerPlayer player,
                                     @NotNull final ItemStack stack,
                                     @NotNull final CommandBlockEntity command,
                                     final boolean ctrlKey)
    {
        final ScanToolData data = ScanToolData.updateItemStack(stack, d -> saveSlot(d, stack, player));
        final ScanToolData.Slot slot = data.currentSlot();

        if (slot.name().isBlank() || slot.name().contains(" "))
        {
            player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.badname"));
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }

        //noinspection StatementWithEmptyBody
        if (command.getCommandBlock().getCommand().isBlank())
        {
            // it's always ok to set a blank command block
        }
        else if (!command.getCommandBlock().getCommand().contains(MOD_ID + " " + ScanCommand.NAME + " "))
        {
            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.badcommand"), false);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        else if (!ctrlKey)
        {
            final StringReader reader = new StringReader(command.getCommandBlock().getCommand());
            if (reader.canRead() && reader.peek() == '/') { reader.read(); }

            final CommandDispatcher<CommandSourceStack> dispatcher = player.getServer().getCommands().getDispatcher();
            final ParseResults<CommandSourceStack> parsed = dispatcher.parse(reader, command.getCommandBlock().createCommandSourceStack());
            if (parsed.getContext().getArguments().containsKey(ScanCommand.FILE_NAME))
            {
                final CommandContext<CommandSourceStack> cmdContext = parsed.getContext().build(parsed.getReader().getString());
                final String currentName = StringArgumentType.getString(cmdContext, ScanCommand.FILE_NAME);
                if (!currentName.equals(slot.name()))
                {
                    player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.different", slot.name(), currentName), false);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_XYLOPHONE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    return;
                }
            }
        }

        final String cmd = ScanCommand.format(slot);
        command.getCommandBlock().setCommand(cmd);

        ScanToolData.updateItemStack(stack, d -> d.withCommandBlock(command));

        player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.ok", slot.name()), false);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Called on both client and server when the player presses the Teleport keybind while holding a scan tool.
     * @param player the player
     * @param stack the scan tool
     * @return (client) true to pass to server, false to drop; (server) don't care
     */
    public boolean onTeleport(@NotNull final Player player, @NotNull final ItemStack stack)
    {
        if (!player.isCreative() || !Structurize.getConfig().getServer().teleportAllowed.get())
        {
            return false;
        }

        final ScanToolData data = ScanToolData.readFromItemStack(stack);
        if (data.commandPos() == null)
        {
            if (player.level().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.teleport.nocmd"), false);
                player.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 1.0F, 1.0F);
            }
            return false;
        }

        if (!player.level().dimension().equals(data.dimension()))
        {
            if (player.level().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.teleport.dimension"), false);
                player.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 1.0F, 1.0F);
            }
            return false;
        }

        final ScanToolData.Slot slot = data.currentSlot();
        if (slot == null)
        {
            if (player.level().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.teleport.noscan"), false);
                player.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 1.0F, 1.0F);
            }
            return false;
        }

        final BlockPos commandPos = data.commandPos().above();
        final BlockPos buildPos = getTeleportPos(slot.box());
        final Level level = player.level();

        final long commandDistance = BlockPosUtil.getDistanceSquared(commandPos, player.blockPosition());
        final long buildDistance = BlockPosUtil.getDistanceSquared(buildPos, player.blockPosition());

        // teleport to whichever is further away of the command block or building
        BlockPos target = commandDistance < buildDistance ? buildPos : commandPos;

        if (Structurize.getConfig().getServer().teleportSafety.get())
        {
            level.getChunk(target); // to force chunk loading for the below
            @Nullable final BlockPos safeTarget = BlockPosUtil.findSafeTeleportPos(level, target, false);
            if (safeTarget == null)
            {
                Log.getLogger().warn("No safe landing for scan-teleport " + player.getName().getString() + " to " + target.toShortString());
                return false;
            }
            target = safeTarget;
        }

        if (target.getY() < level.getMinBuildHeight() + 2)
        {
            // safety abort if we would teleport to bedrock or below (which can happen if the heightmap check fails)
            Log.getLogger().warn("Aborting attempt to scan-teleport " + player.getName().getString() + " to " + target.toShortString());
            return false;
        }

        for (int i = 0; i < 32; ++i)
        {
            level.addParticle(ParticleTypes.PORTAL, player.blockPosition().getX(), player.blockPosition().getY() + level.getRandom().nextDouble() * 2.0D, player.blockPosition().getZ(), level.getRandom().nextGaussian(), 0.0D, level.getRandom().nextGaussian());
            level.addParticle(ParticleTypes.PORTAL, target.getX(), target.getY() + level.getRandom().nextDouble() * 2.0D, target.getZ(), level.getRandom().nextGaussian(), 0.0D, level.getRandom().nextGaussian());
        }

        if (player.level() instanceof ServerLevel serverLevel)
        {
            player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

            final CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO, serverLevel, 2,
                    player.getName().getString(), stack.getDisplayName(), serverLevel.getServer(), player);
            final CommandDispatcher<CommandSourceStack> dispatcher = serverLevel.getServer().getCommands().getDispatcher();
            try
            {
                dispatcher.execute(String.format("teleport %s %f %f %f", player.getUUID(), target.getX() + 0.5, target.getY() + 0.0, target.getZ() + 0.5), source);
            }
            catch (Exception e)
            {
                Log.getLogger().error("Command tool teleport failed", e);
            }

            player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
            player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return true;
    }

    /**
     * Determines the best position to teleport to, near the given scan bounds.
     * This can't assume that the area is loaded, so it has to guess something appropriate.
     *
     * It's usually most convenient to appear outside the scanned area (it might be filled with placeholders),
     * so we just pick an arbitrary location on a particular side that's convenient.
     *
     * @param box The scan area.
     * @return A convenient teleportation position near but not inside the scan area.
     */
    @NotNull
    private BlockPos getTeleportPos(@NotNull final BoxPreviewData box)
    {
        final Direction direction = Structurize.getConfig().getServer().teleportBuildDirection.get();
        final int offset = Structurize.getConfig().getServer().teleportBuildDistance.get();

        final AABB bounds = AABB.encapsulatingFullBlocks(box.pos1(), box.pos2());
        final int size = (int) Math.round(bounds.max(direction.getAxis()) - bounds.min(direction.getAxis()));

        return BlockPos.containing(bounds.getCenter()).atY((int) bounds.minY).relative(direction, offset + size / 2);
    }

    /**
     * Gets the coordinates of this tool as a {@link BoxPreviewData}
     * @param tool The tool stack (assumed already been validated)
     * @param player The player who will be notified if it has a bad anchor position
     * @return the box
     */
    @Nullable
    public static BoxPreviewData getBox(@NotNull final ItemStack tool, @NotNull final Player player)
    {
        final PosSelection tag = PosSelection.readFromItemStack(tool);
        if (!tag.hasSelection())
        {
            return null;
        }
        Optional<BlockPos> anchor = ScanToolData.readFromItemStack(tool).currentSlot().box().anchor();
        if (anchor.isPresent() && !BlockPosUtil.isInbetween(anchor.get(), tag.startPos().get(), tag.endPos().get()))
        {
            if (player.level().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.outsideanchor"), false);
            }
            anchor = Optional.empty();
        }
        return new BoxPreviewData(tag.startPos().get(), tag.endPos().get(), anchor);
    }

    /**
     * Saves the anchor coordinates on this stack.
     * @param tool The tool stack (assumed already been validated)
     * @param anchor The new anchor position (or null to clear)
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static void setAnchorPos(@NotNull final ItemStack tool,
                                    @Nullable final BlockPos anchor)
    {
        ScanToolData.updateItemStack(tool, data ->
        {
            final BoxPreviewData oldBox = data.currentSlot().box();
            final BoxPreviewData newBox = oldBox.withAnchor(Optional.ofNullable(anchor));
            return data.withCurrentSlot(data.currentSlot().withBox(newBox));
        });
    }

    /**
     * Loads the anchor coordinates from this stack.
     * @param tool The tool stack (assumed already been validated)
     * @return the anchor position or null
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "1.21")
    public static BlockPos getAnchorPos(@NotNull final ItemStack tool)
    {
        return ScanToolData.readFromItemStack(tool).currentSlot().box().anchor().orElse(null);
    }

    /**
     * Saves the structure name on this stack.
     * @param tool The tool stack (assumed already validated)
     * @param name The structure name (or null/empty to clear)
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static void setStructureName(@NotNull final ItemStack tool,
                                        @Nullable String name)
    {
        ScanToolData.updateItemStack(tool, data -> data.withCurrentSlot(data.currentSlot().withName(name == null ? "" : name)));
    }

    /**
     * Gets the structure name saved on this stack.
     * @param tool The tool stack (assumed already validated)
     * @return The structure name (or empty string)
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public static String getStructureName(@NotNull final ItemStack tool)
    {
        return ScanToolData.readFromItemStack(tool).currentSlot().name();
    }
}
