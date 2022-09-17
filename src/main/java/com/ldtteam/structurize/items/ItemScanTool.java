package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.IMiddleClickableItem;
import com.ldtteam.structurize.api.util.IScrollableItem;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.client.gui.WindowScan;
import com.ldtteam.structurize.commands.ScanCommand;
import com.ldtteam.structurize.network.messages.SaveScanMessage;
import com.ldtteam.structurize.network.messages.ShowScanMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.ScanToolData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.ANCHOR_POS_OUTSIDE_SCHEMATIC;
import static com.ldtteam.structurize.api.util.constant.TranslationConstants.MAX_SCHEMATIC_SIZE_REACHED;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * Item used to scan structures.
 */
public class ItemScanTool extends AbstractItemWithPosSelector implements IScrollableItem, IMiddleClickableItem
{
    private static final String ANCHOR_POS_TKEY = "item.possetter.anchorpos";
    private static final String NBT_ANCHOR_POS  = "structurize:anchor_pos";
    private static final String NBT_NAME = "structurize:name";
    private static final String NBT_COMMAND_POS = "structurize:cmd_pos";
    private static final String NBT_DIMENSION = "structurize:dim";

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
        final ScanToolData data = new ScanToolData(itemStack.getOrCreateTag());
        saveSlot(data, itemStack, playerIn);

        if (!worldIn.isClientSide)
        {
            if (playerIn.isShiftKeyDown())
            {
                saveStructure(worldIn, playerIn, data.getCurrentSlotData(), true);
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
        if (slot.getBox().getAnchor().isPresent())
        {
            if (!BlockPosUtil.isInbetween(slot.getBox().getAnchor().get(), slot.getBox().getPos1(), slot.getBox().getPos2()))
            {
                LanguageHandler.sendPlayerMessage(player, ANCHOR_POS_OUTSIDE_SCHEMATIC);
                return;
            }
        }

        final BoundingBox box = BoundingBox.fromCorners(slot.getBox().getPos1(), slot.getBox().getPos2());
        if (box.getXSpan() * box.getYSpan() * box.getZSpan() > Structurize.getConfig().getServer().schematicBlockLimit.get())
        {
            LanguageHandler.sendPlayerMessage(player, MAX_SCHEMATIC_SIZE_REACHED, Structurize.getConfig().getServer().schematicBlockLimit.get());
            return;
        }

        final long currentMillis = System.currentTimeMillis();
        final String currentMillisString = Long.toString(currentMillis);
        String fileName;
        if (slot.getName().isEmpty())
        {
            fileName = Component.translatable("item.sceptersteel.scanformat", "", currentMillisString).getString();
        }
        else
        {
            fileName = slot.getName();
        }

        if (!fileName.contains(".blueprint"))
        {
            fileName+= ".blueprint";
        }

        final BlockPos zero = new BlockPos(box.minX(), box.minY(), box.minZ());
        final Blueprint bp = BlueprintUtil.createBlueprint(world, zero, saveEntities, (short) box.getXSpan(), (short) box.getYSpan(), (short) box.getZSpan(), fileName, slot.getBox().getAnchor());

        if (slot.getBox().getAnchor().isEmpty() && bp.getPrimaryBlockOffset().equals(new BlockPos(bp.getSizeX() / 2, 0, bp.getSizeZ() / 2)))
        {
            final List<BlockInfo> list = bp.getBlockInfoAsList().stream()
              .filter(blockInfo -> blockInfo.hasTileEntityData() && blockInfo.getTileEntityData().contains(TAG_BLUEPRINTDATA))
              .collect(Collectors.toList());

            if (list.size() > 1)
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.scanbadanchor", fileName), false);
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
                setBounds(itemstack, start, end);
            }
        }

        setAnchorPos(itemstack, pos);
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack,
                                @Nullable Level world,
                                @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flags)
    {
        super.appendHoverText(stack, world, tooltip, flags);

        if (stack.hasTag())
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
        final ScanToolData data = new ScanToolData(stack.getOrCreateTag());
        MutableComponent desc = Component.empty()
                .append(Component.literal(String.valueOf(data.getCurrentSlotId())).withStyle(ChatFormatting.GRAY));

        final String name = getStructureName(stack);
        if (!name.isEmpty())
        {
            desc = desc.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(name));
        }

        return desc;
    }

    @Override
    public InteractionResult onMiddleClick(@NotNull final Player player,
                                           @NotNull final ItemStack stack,
                                           @Nullable final BlockPos pos,
                                           final int modifiers)
    {
        if (pos == null)
        {
            // treat click in air like mouse scrolling (just in case someone doesn't have a wheel)
            final double delta = player.isShiftKeyDown() ? -1 : 1;
            return onMouseScroll(player, stack, delta);
        }

        if (player.getLevel().getBlockEntity(pos) instanceof CommandBlockEntity command)
        {
            return onCommandBlockClick(player, stack, command, modifiers);
        }

        // ignore middle click on blocks for now (standard pick-block)
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onMouseScroll(@NotNull final Player player,
                                           @NotNull final ItemStack stack,
                                           final double delta)
    {
        if (player.getLevel().isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        switchSlot((ServerPlayer) player, stack, delta < 0 ? ScanToolData::prevSlot : ScanToolData::nextSlot);

        return InteractionResult.SUCCESS;
    }

    private void switchSlot(@NotNull final ServerPlayer player,
                            @NotNull final ItemStack stack,
                            @NotNull final Consumer<ScanToolData> action)
    {
        final ScanToolData data = new ScanToolData(stack.getOrCreateTag());
        saveSlot(data, stack, player);
        action.accept(data);
        final ScanToolData.Slot slot = loadSlot(data, stack);

        Network.getNetwork().sendToPlayer(new ShowScanMessage(slot.getBox()), player);
    }

    private void saveSlot(@NotNull final ScanToolData data,
                          @NotNull final ItemStack stack,
                          @NotNull final Player player)
    {
        data.setCurrentSlotData(new ScanToolData.Slot(getStructureName(stack), getBox(stack, player)));
    }

    public ScanToolData.Slot loadSlot(@NotNull final ScanToolData data,
                                      @NotNull final ItemStack stack)
    {
        final ScanToolData.Slot slot = data.getCurrentSlotData();

        // this seems a little silly at first, duplicating this info outside the slot storage.
        // but it preserves compatibility with AbstractItemWithPosSelector.
        setStructureName(stack, slot.getName());
        setBounds(stack, slot.getBox().getPos1(), slot.getBox().getPos2());
        setAnchorPos(stack, slot.getBox().getAnchor().orElse(null));

        return slot;
    }

    /**
     * Called on both client and server side when [shift-]middle-clicking a Command Block.
     * @param player the player
     * @param stack the scan tool
     * @param command the command block entity
     * @param modifiers the modifier keys
     * @return PASS to do the normal action, SUCCESS to pass to server, FAILURE to stop
     */
    private InteractionResult onCommandBlockClick(@NotNull final Player player,
                                                  @NotNull final ItemStack stack,
                                                  @NotNull final CommandBlockEntity command,
                                                  final int modifiers)
    {
        if (!player.isCreative())
        {
            return InteractionResult.PASS;
        }
        if (player.getLevel().isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown())
        {
            onCommandBlockPaste((ServerPlayer) player, stack, command, modifiers);
        }
        else
        {
            onCommandBlockCopy((ServerPlayer) player, stack, command, modifiers);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Called on server side when middle-clicking a command block.
     * @param player the player
     * @param stack the scan tool
     * @param command the command block
     * @param modifiers the modifier keys
     */
    private void onCommandBlockCopy(@NotNull final ServerPlayer player,
                                    @NotNull final ItemStack stack,
                                    @NotNull final CommandBlockEntity command,
                                    final int modifiers)
    {
        final StringReader reader = new StringReader(command.getCommandBlock().getCommand());
        if (reader.canRead() && reader.peek() == '/') { reader.read(); }

        final CommandDispatcher<CommandSourceStack> dispatcher = player.getLevel().getServer().getCommands().getDispatcher();
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
            Optional<BlockPos> anchor = Optional.empty();
            if (parsed.getContext().getArguments().containsKey(ScanCommand.ANCHOR_POS))
            {
                anchor = Optional.of(BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.ANCHOR_POS));
            }
            String name = "";
            if (parsed.getContext().getArguments().containsKey(ScanCommand.FILE_NAME))
            {
                name = StringArgumentType.getString(cmdContext, ScanCommand.FILE_NAME);
            }

            stack.getOrCreateTag().put(NBT_COMMAND_POS, NbtUtils.writeBlockPos(command.getBlockPos()));
            stack.getOrCreateTag().putString(NBT_DIMENSION, command.getLevel().dimension().location().toString());

            final ScanToolData data = new ScanToolData(stack.getOrCreateTag());
            data.setCurrentSlotData(new ScanToolData.Slot(name, new BoxPreviewData(from, to, anchor)));
            final ScanToolData.Slot slot = loadSlot(data, stack);
            Network.getNetwork().sendToPlayer(new ShowScanMessage(slot.getBox()), player);

            player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.copy.ok", name), false);
            player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
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
     * @param modifiers the modifier keys
     */
    private void onCommandBlockPaste(@NotNull final ServerPlayer player,
                                     @NotNull final ItemStack stack,
                                     @NotNull final CommandBlockEntity command,
                                     final int modifiers)
    {
        final ScanToolData data = new ScanToolData(stack.getOrCreateTag());
        saveSlot(data, stack, player);
        final ScanToolData.Slot slot = data.getCurrentSlotData();

        if (slot.getName().isBlank() || slot.getName().contains(" "))
        {
            player.sendSystemMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.badname"));
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BIT, SoundSource.PLAYERS, 1.0F, 1.0F);
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
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        else if ((modifiers & GLFW.GLFW_MOD_CONTROL) == 0)
        {
            final StringReader reader = new StringReader(command.getCommandBlock().getCommand());
            if (reader.canRead() && reader.peek() == '/') { reader.read(); }

            final CommandDispatcher<CommandSourceStack> dispatcher = player.getServer().getCommands().getDispatcher();
            final ParseResults<CommandSourceStack> parsed = dispatcher.parse(reader, command.getCommandBlock().createCommandSourceStack());
            if (parsed.getContext().getArguments().containsKey(ScanCommand.FILE_NAME))
            {
                final CommandContext<CommandSourceStack> cmdContext = parsed.getContext().build(parsed.getReader().getString());
                final String currentName = StringArgumentType.getString(cmdContext, ScanCommand.FILE_NAME);
                if (!currentName.equals(slot.getName()))
                {
                    player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.different", slot.getName(), currentName), false);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_XYLOPHONE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    return;
                }
            }
        }

        final String cmd = ScanCommand.format(slot);
        command.getCommandBlock().setCommand(cmd);

        stack.getOrCreateTag().put(NBT_COMMAND_POS, NbtUtils.writeBlockPos(command.getBlockPos()));
        stack.getOrCreateTag().putString(NBT_DIMENSION, command.getLevel().dimension().location().toString());

        player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.paste.ok", slot.getName()), false);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Called on both client and server when the player presses the Teleport keybind while holding a scan tool.
     * @param player the player
     * @param stack the scan tool
     * @return (client) true to pass to server, false to drop; (server) don't care
     */
    public boolean onTeleport(@NotNull final Player player, @NotNull final ItemStack stack)
    {
        if (!player.isCreative())
        {
            return false;
        }

        if (stack.getTag() == null || !stack.getTag().contains(NBT_COMMAND_POS))
        {
            if (player.getLevel().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.teleport.nocmd"), false);
                player.playSound(SoundEvents.NOTE_BLOCK_BIT, 1.0F, 1.0F);
            }
            return false;
        }

        if (!player.getLevel().dimension().location().toString().equals(stack.getTag().getString(NBT_DIMENSION)))
        {
            if (player.getLevel().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.teleport.dimension"), false);
                player.playSound(SoundEvents.NOTE_BLOCK_BIT, 1.0F, 1.0F);
            }
            return false;
        }

        final ScanToolData data = new ScanToolData(stack.getTag());
        final ScanToolData.Slot slot = data.getCurrentSlotData();

        if (slot.getBox().getPos1().equals(BlockPos.ZERO) && slot.getBox().getPos2().equals(BlockPos.ZERO))
        {
            if (player.getLevel().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.teleport.noscan"), false);
                player.playSound(SoundEvents.NOTE_BLOCK_BIT, 1.0F, 1.0F);
            }
            return false;
        }

        final BlockPos commandPos = NbtUtils.readBlockPos(stack.getOrCreateTag().getCompound(NBT_COMMAND_POS)).above();
        final BlockPos buildPos = getTeleportPos(slot.getBox());
        final Level level = player.getLevel();

        final long commandDistance = BlockPosUtil.getDistanceSquared(commandPos, player.blockPosition());
        final long buildDistance = BlockPosUtil.getDistanceSquared(buildPos, player.blockPosition());

        // teleport to whichever is further away of the command block or building
        BlockPos target = commandDistance < buildDistance ? buildPos : commandPos;
        final ChunkAccess chunk = level.getChunk(target); // to force chunk loading for the below
        @Nullable final BlockPos safeTarget = BlockPosUtil.findSafeTeleportPos(level, target, false);
        if (safeTarget == null)
        {
            Log.getLogger().warn("No safe landing for scan-teleport " + player.getName().getString() + " to " + target.toShortString());
            return false;
        }
        target = safeTarget;

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

        if (player.getLevel() instanceof ServerLevel serverLevel)
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
        final Direction direction = Direction.SOUTH;    // these could potentially be settings
        final int offset = 3;

        final AABB bounds = new AABB(box.getPos1(), box.getPos2());
        final int size = (int) Math.round(bounds.max(direction.getAxis()) - bounds.min(direction.getAxis()));

        return new BlockPos(bounds.getCenter()).atY((int) bounds.minY).relative(direction, offset + size / 2);
    }

    /**
     * Gets the coordinates of this tool as a {@link BoxPreviewData}
     * @param tool The tool stack (assumed already been validated)
     * @param player The player who will be notified if it has a bad anchor position
     * @return the box
     */
    public static BoxPreviewData getBox(@NotNull final ItemStack tool, @NotNull final Player player)
    {
        final Tuple<BlockPos, BlockPos> bounds = getBounds(tool);
        Optional<BlockPos> anchor = Optional.ofNullable(getAnchorPos(tool));
        if (anchor.isPresent() && !BlockPosUtil.isInbetween(anchor.get(), bounds.getA(), bounds.getB()))
        {
            if (player.getLevel().isClientSide())
            {
                player.displayClientMessage(Component.translatable("com.ldtteam.structurize.gui.scantool.outsideanchor"), false);
            }
            anchor = Optional.empty();
        }
        return new BoxPreviewData(bounds.getA(), bounds.getB(), anchor);
    }

    /**
     * Saves the anchor coordinates on this stack.
     * @param tool The tool stack (assumed already been validated)
     * @param anchor The new anchor position (or null to clear)
     */
    public static void setAnchorPos(@NotNull final ItemStack tool,
                                    @Nullable final BlockPos anchor)
    {
        if (anchor == null)
        {
            tool.getOrCreateTag().remove(NBT_ANCHOR_POS);
        }
        else
        {
            tool.getOrCreateTag().put(NBT_ANCHOR_POS, NbtUtils.writeBlockPos(anchor));
        }
    }

    /**
     * Loads the anchor coordinates from this stack.
     * @param tool The tool stack (assumed already been validated)
     * @return the anchor position or null
     */
    @Nullable
    public static BlockPos getAnchorPos(@NotNull final ItemStack tool)
    {
        final CompoundTag tag = tool.getOrCreateTag();
        return tag.contains(NBT_ANCHOR_POS) ? NbtUtils.readBlockPos(tag.getCompound(NBT_ANCHOR_POS)) : null;
    }

    /**
     * Saves the structure name on this stack.
     * @param tool The tool stack (assumed already validated)
     * @param name The structure name (or null/empty to clear)
     */
    public static void setStructureName(@NotNull final ItemStack tool,
                                        @Nullable final String name)
    {
        if (name == null || name.isEmpty())
        {
            tool.getOrCreateTag().remove(NBT_NAME);
        }
        else
        {
            tool.getOrCreateTag().putString(NBT_NAME, name);
        }
    }

    /**
     * Gets the structure name saved on this stack.
     * @param tool The tool stack (assumed already validated)
     * @return The structure name (or empty string)
     */
    public static String getStructureName(@NotNull final ItemStack tool)
    {
        return tool.getOrCreateTag().getString(NBT_NAME);
    }
}
