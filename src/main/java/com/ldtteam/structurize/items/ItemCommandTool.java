package com.ldtteam.structurize.items;

import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.commands.ScanCommand;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.network.messages.ShowScanMessage;
import com.ldtteam.structurize.network.messages.UpdateScanCommandBlockMessage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.MOD_ID;

/**
 * The Command Tool is an item that allows creative players to more easily use and update
 * auto-scanner command blocks when building schematics.
 */
public class ItemCommandTool extends Item
{
    private static final String NBT_COMMAND_POS  = "structurize:command_pos";
    private static final String NBT_BUILD_POS  = "structurize:build_pos";
    private static final String NBT_DIMENSION  = "structurize:dim";
    private static final String NBT_NAME  = "structurize:name";

    /**
     * Creates default command tool item.
     *
     * @param itemGroup creative tab
     */
    public ItemCommandTool(final CreativeModeTab itemGroup)
    {
        super(new Item.Properties().durability(0).setNoRepair().rarity(Rarity.RARE).tab(itemGroup));
    }

    /**
     * Updates the teleport data on this tool.
     * @param tool The tool to update (assumed already valid).
     * @param level The level containing the command block and build.
     * @param commandPos The position of the command block.
     * @param buildPos The position of the build.
     * @param name The name of the build.
     */
    public static void setData(@NotNull final ItemStack tool,
                               @NotNull final Level level,
                               @NotNull final BlockPos commandPos,
                               @NotNull final BlockPos buildPos,
                               @NotNull final String name)
    {
        final CompoundTag compound = tool.getOrCreateTag();
        compound.put(NBT_COMMAND_POS, NbtUtils.writeBlockPos(commandPos));
        compound.put(NBT_BUILD_POS, NbtUtils.writeBlockPos(buildPos));
        compound.putString(NBT_DIMENSION, level.dimension().location().toString());
        compound.putString(NBT_NAME, name);
    }

    /**
     * Right-click in air => teleport from command block to scanned building or back.
     */
    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull final Level worldIn,
                                                  @NotNull final Player playerIn,
                                                  @NotNull final InteractionHand handIn)
    {
        final ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (!playerIn.isCreative())
        {
            if (worldIn.isClientSide())
            {
                playerIn.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.creativeonly"), playerIn.getUUID());
            }

            return InteractionResultHolder.fail(itemstack);
        }

        assert itemstack.getTag() != null;  // just to quiet the warning
        if (!itemstack.hasTag() || !itemstack.getTag().contains(NBT_COMMAND_POS) || !itemstack.getTag().contains(NBT_BUILD_POS) || !itemstack.getTag().contains(NBT_DIMENSION))
        {
            if (worldIn.isClientSide())
            {
                playerIn.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.teleportnocommand"), playerIn.getUUID());
            }

            return InteractionResultHolder.fail(itemstack);
        }

        if (!worldIn.dimension().location().toString().equals(itemstack.getTag().getString(NBT_DIMENSION)))
        {
            if (worldIn.isClientSide())
            {
                playerIn.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.teleportwrongdimension"), playerIn.getUUID());
                playerIn.playSound(SoundEvents.NOTE_BLOCK_BIT, 1.0F, 1.0F);
            }

            return InteractionResultHolder.fail(itemstack);
        }

        final BlockPos commandPos = NbtUtils.readBlockPos(itemstack.getTag().getCompound(NBT_COMMAND_POS));
        final BlockPos buildPos = NbtUtils.readBlockPos(itemstack.getTag().getCompound(NBT_BUILD_POS));
        final long commandDistance = BlockPosUtil.getDistanceSquared(commandPos, playerIn.blockPosition());
        final long buildDistance = BlockPosUtil.getDistanceSquared(buildPos, playerIn.blockPosition());

        // teleport to whichever is further away of the command block or building
        BlockPos target = commandDistance < buildDistance ? buildPos : commandPos.above();
        final ChunkAccess chunk = worldIn.getChunk(target); // to force chunk loading for the below
        target = worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target).above();

        for (int i = 0; i < 32; ++i)
        {
            worldIn.addParticle(ParticleTypes.PORTAL, playerIn.blockPosition().getX(), playerIn.blockPosition().getY() + worldIn.getRandom().nextDouble() * 2.0D, playerIn.blockPosition().getZ(), worldIn.getRandom().nextGaussian(), 0.0D, worldIn.getRandom().nextGaussian());
            worldIn.addParticle(ParticleTypes.PORTAL, target.getX(), target.getY() + worldIn.getRandom().nextDouble() * 2.0D, target.getZ(), worldIn.getRandom().nextGaussian(), 0.0D, worldIn.getRandom().nextGaussian());
        }

        if (worldIn instanceof ServerLevel serverLevel)
        {
            // teleport effect for everyone else at the original coordinates
            playerIn.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

            final CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, playerIn.position(), Vec2.ZERO, serverLevel, 2,
                    playerIn.getName().getString(), itemstack.getDisplayName(), serverLevel.getServer(), playerIn);
            final CommandDispatcher<CommandSourceStack> dispatcher = serverLevel.getServer().getCommands().getDispatcher();
            try
            {
                dispatcher.execute(String.format("teleport %s %f %f %f", playerIn.getUUID(), target.getX() + 0.5, target.getY() + 0.0, target.getZ() + 0.5), source);
            }
            catch (Exception e)
            {
                Log.getLogger().error("Command tool teleport failed", e);
            }
        }

        // and effect for everyone at the new coordinates
        playerIn.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

        return InteractionResultHolder.success(itemstack);
    }

    /**
     * Shift-right-click tool on command block => copy command to scan
     */
    @NotNull
    @Override
    public InteractionResult useOn(@NotNull final UseOnContext context)
    {
        final BlockPos pos = context.getClickedPos();
        final BlockEntity entity = context.getLevel().getBlockEntity(pos);
        final Player player = context.getPlayer();
        final boolean isClientSide = context.getLevel().isClientSide();

        if (player == null || !player.isCreative())
        {
            if (player != null && isClientSide)
            {
                player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.creativeonly"), player.getUUID());
            }
            return InteractionResult.FAIL;
        }

        if (!(entity instanceof CommandBlockEntity command))
        {
            if (isClientSide)
            {
                player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.copycommand"), player.getUUID());
            }
            return InteractionResult.FAIL;
        }

        final ItemStack scanTool = findScanTool(player);
        if (scanTool.isEmpty())
        {
            if (isClientSide)
            {
                player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.copycommand.noscantool"), player.getUUID());
            }
            return InteractionResult.FAIL;
        }

        if (isClientSide)
        {
            return InteractionResult.SUCCESS;
        }

        final StringReader reader = new StringReader(command.getCommandBlock().getCommand());
        if (reader.canRead() && reader.peek() == '/') { reader.read(); }

        final CommandDispatcher<CommandSourceStack> dispatcher = ((ServerLevel)context.getLevel()).getServer().getCommands().getDispatcher();
        final ParseResults<CommandSourceStack> parsed = dispatcher.parse(reader, command.getCommandBlock().createCommandSourceStack());
        if (parsed.getReader().canRead() || parsed.getContext().getNodes().size() < 4
                || !parsed.getContext().getNodes().get(0).getNode().getName().equals(MOD_ID)
                || !parsed.getContext().getNodes().get(1).getNode().getName().equals(ScanCommand.NAME))
        {
            player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.copycommand.notscan"), player.getUUID());
            return InteractionResult.FAIL;
        }

        final CommandContext<CommandSourceStack> cmdContext = parsed.getContext().build(parsed.getReader().getString());
        try
        {
            final BlockPos from = BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.POS1);
            final BlockPos to = BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.POS2);
            BlockPos anchor = null;
            if (parsed.getContext().getArguments().containsKey(ScanCommand.ANCHOR_POS))
            {
                anchor = BlockPosArgument.getSpawnablePos(cmdContext, ScanCommand.ANCHOR_POS);
            }
            String name = "";
            if (parsed.getContext().getArguments().containsKey(ScanCommand.FILE_NAME))
            {
                name = StringArgumentType.getString(cmdContext, ScanCommand.FILE_NAME);
            }

            setData(context.getItemInHand(), context.getLevel(), pos, getTeleportPos(from, to), name);

            ItemScanTool.setBounds(scanTool, from, to);
            ItemScanTool.setAnchorPos(scanTool, anchor);
            Network.getNetwork().sendToPlayer(new ShowScanMessage(from, to, anchor, name), (ServerPlayer) player);

            player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.copycommand.ok", name), player.getUUID());
            player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        catch (CommandSyntaxException e)
        {
            player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.copycommand.notscan"), player.getUUID());
            return InteractionResult.FAIL;
        }
    }

    /**
     * Searches the player's hotbar (only) for a scan tool.
     * If there's multiple, it prefers the closest item to the current slot, starting from the left side.
     * @param player The player to search.
     * @return The scan tool found, or an empty stack.
     */
    @NotNull
    private ItemStack findScanTool(@NotNull final Player player)
    {
        final int current = player.getInventory().selected;
        for (int i = 1; i < 9; ++i)
        {
            for (int j = -1; j <= 1; j += 2)
            {
                final int slot = current + i * j;
                if (slot < 0 || slot > 9) continue;

                final ItemStack stack = player.getInventory().getItem(slot);
                if (stack.is(ModItems.scanTool.get()))
                {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Determines the best position to teleport to, near the given scan bounds.
     * This can't assume that the area is loaded, so it has to guess something appropriate.
     *
     * It's usually most convenient to appear outside the scanned area (it might be filled with placeholders),
     * so we just pick an arbitrary location on a particular side that's convenient.  Perhaps this
     * could be made into a setting.
     *
     * @param from The first bounding corner.
     * @param to The second bounding corner.
     * @return A convenient teleportation position near but not inside the scan area.
     */
    @NotNull
    private static BlockPos getTeleportPos(@NotNull final BlockPos from,
                                           @NotNull final BlockPos to)
    {
        final Direction direction = Direction.SOUTH;
        final int offset = 3;

        final AABB bounds = new AABB(from, to);
        final int size = (int) Math.round(bounds.max(direction.getAxis()) - bounds.min(direction.getAxis()));

        return new BlockPos(bounds.getCenter()).atY((int) bounds.minY).relative(direction, offset + size / 2);
    }

    /**
     * Left-click on command block => paste current scan into block (and also update coords)
     */
    @Override
    public boolean canAttackBlock(@NotNull final BlockState blockState,
                                  @NotNull final Level world,
                                  @NotNull final BlockPos pos,
                                  @NotNull final Player player)
    {
        if (player.isCreative() && player.getLevel().getBlockEntity(pos) instanceof CommandBlockEntity)
        {
            if (world.isClientSide())
            {
                if (Settings.instance.getBox() == null)
                {
                    player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.pastenoscan"), player.getUUID());
                    return false;
                }

                final BlockPos from = Settings.instance.getBox().getA();
                final BlockPos to = Settings.instance.getBox().getB();
                final BlockPos anchor = Settings.instance.getAnchorPos().orElse(null);

                String name = Settings.instance.getStructureName();
                Network.getNetwork().sendToServer(new UpdateScanCommandBlockMessage(pos, from, to, anchor, name, player.isShiftKeyDown()));
            }

            return false;
        }

        return super.canAttackBlock(blockState, world, pos, player);
    }

    /**
     * Actually perform the server-side update of the command block.
     * @param player The player requesting the update.
     * @param pos The location of the command block.
     * @param from The first corner of the scan.
     * @param to The second corner of the scan.
     * @param anchor The anchor position (or null).
     * @param fileName The scan filename.
     * @param force Force overwriting the command even if it seems dodgy.
     */
    public static void pasteCommandBlock(@Nullable final ServerPlayer player,
                                         @NotNull final BlockPos pos,
                                         @NotNull final BlockPos from,
                                         @NotNull final BlockPos to,
                                         @Nullable final BlockPos anchor,
                                         @NotNull final String fileName,
                                         final boolean force)
    {
        if (player == null || !player.isCreative()) return;
        if (!(player.getLevel().getBlockEntity(pos) instanceof CommandBlockEntity command))
        {
            return;
        }

        if (fileName.isBlank() || fileName.contains(" "))
        {
            player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.pastebadname"), player.getUUID());
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
            player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.pastebadcommand"), player.getUUID());
            player.playNotifySound(SoundEvents.NOTE_BLOCK_BIT, SoundSource.PLAYERS, 1.0F, 1.0F);
            return;
        }
        else if (!force)
        {
            final StringReader reader = new StringReader(command.getCommandBlock().getCommand());
            if (reader.canRead() && reader.peek() == '/') { reader.read(); }

            final CommandDispatcher<CommandSourceStack> dispatcher = player.getServer().getCommands().getDispatcher();
            final ParseResults<CommandSourceStack> parsed = dispatcher.parse(reader, command.getCommandBlock().createCommandSourceStack());
            if (parsed.getContext().getArguments().containsKey(ScanCommand.FILE_NAME))
            {
                final CommandContext<CommandSourceStack> cmdContext = parsed.getContext().build(parsed.getReader().getString());
                final String currentName = StringArgumentType.getString(cmdContext, ScanCommand.FILE_NAME);
                if (!currentName.equals(fileName))
                {
                    player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.pastedifferent", fileName, currentName), player.getUUID());
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_XYLOPHONE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    return;
                }
            }
        }

        if (player.getMainHandItem().is(ModItems.commandTool.get()))
        {
            setData(player.getMainHandItem(), player.getLevel(), pos, getTeleportPos(from, to), fileName);
        }

        final String cmd = ScanCommand.format(from, to, anchor, fileName);
        command.getCommandBlock().setCommand(cmd);

        player.sendMessage(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.pasteok", fileName), player.getUUID());
        player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Instant clicks in survival (even though we're disabled there).
     */
    @Override
    public float getDestroySpeed(@NotNull final ItemStack stack,
                                 @NotNull final BlockState state)
    {
        return Float.MAX_VALUE;
    }

    @Override
    public int getItemStackLimit(@NotNull final ItemStack stack)
    {
        return 1;
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
            assert stack.getTag() != null;
            if (stack.getTag().contains(NBT_NAME))
            {
                tooltip.add(new TranslatableComponent("com.ldtteam.structurize.gui.commandtool.copied", stack.getTag().getString(NBT_NAME)));
            }
        }
    }
}
