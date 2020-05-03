package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.items.ItemScanTool;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Command for opening WindowScanTool or scanning a structure into a file
 */
public class ScanCommand extends AbstractCommand
{
    /**
     * Descriptive string.
     */
    public final static String NAME = "scan";

    /**
     * The player not found for a scan message.
     */
    private static final String PLAYER_NOT_FOUND = "com.structurize.command.playernotfound";

    /**
     * The scan success message.
     */
    private static final String SCAN_SUCCESS_MESSAGE = "com.structurize.command.scan.success";

    /**
     * The no permission message reply.
     */
    private static final String NO_PERMISSION_MESSAGE = "com.structurize.command.scan.no.perm";

    /**
     * The filename command argument.
     */
    private static final String FILE_NAME = "filename";

    /**
     * The player name command argument.
     */
    private static final String PLAYER_NAME = "player";

    /**
     * Position 1 command argument.
     */
    private static final String POS1 = "pos1";

    /**
     * Position 2 command argument.
     */
    private static final String POS2 = "pos2";

    /**
     * Anchor position command argument.
     */
    private static final String ANCHOR_POS = "anchor_pos";

    private static int execute(final CommandSource source, final BlockPos from, final BlockPos to, final Optional<BlockPos> anchorPos, final GameProfile profile, final String name) throws CommandSyntaxException
    {
        @Nullable final World world = source.getWorld();
        if (source.getEntity() instanceof PlayerEntity && !source.asPlayer().isCreative())
        {
            source.sendErrorMessage(new StringTextComponent(NO_PERMISSION_MESSAGE));
        }

        final PlayerEntity player;
        if (profile != null && world.getServer() != null)
        {
            player = world.getServer().getPlayerList().getPlayerByUUID(profile.getId());
            if (player == null)
            {
                source.sendErrorMessage(new TranslationTextComponent(PLAYER_NOT_FOUND, profile.getName()));
                return 0;
            }
        } 
        else if (source.getEntity() instanceof PlayerEntity)
        {
            player = source.asPlayer();
        } 
        else
        {
            source.sendErrorMessage(new TranslationTextComponent(PLAYER_NOT_FOUND));
            return 0;
        }

        ItemScanTool.saveStructure(world, from, to, player, name == null ? "" : name, true, anchorPos);
        source.sendErrorMessage(new TranslationTextComponent(SCAN_SUCCESS_MESSAGE));
        return 1;
    }

    private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getBlockPos(context, POS1);
        final BlockPos to = BlockPosArgument.getBlockPos(context, POS2);
        return execute(context.getSource(), from, to, Optional.empty(), null, null);
    }

    private static int onExecuteWithAnchor(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getBlockPos(context, POS1);
        final BlockPos to = BlockPosArgument.getBlockPos(context, POS2);
        final BlockPos anchorPos = BlockPosArgument.getBlockPos(context, ANCHOR_POS);
        return execute(context.getSource(), from, to, Optional.of(anchorPos), null, null);
    }

    private static int onExecuteWithPlayerName(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getBlockPos(context, POS1);
        final BlockPos to = BlockPosArgument.getBlockPos(context, POS2);
        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        return execute(context.getSource(), from, to, Optional.empty(), profile, null);
    }

    private static int onExecuteWithPlayerNameAndFileName(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getBlockPos(context, POS1);
        final BlockPos to = BlockPosArgument.getBlockPos(context, POS2);
        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        String name = StringArgumentType.getString(context, FILE_NAME);
        return execute(context.getSource(), from, to, Optional.empty(), profile, name);
    }

    private static int onExecuteWithPlayerNameAndFileNameAndAnchorPos(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getBlockPos(context, POS1);
        final BlockPos to = BlockPosArgument.getBlockPos(context, POS2);
        final BlockPos anchorPos = BlockPosArgument.getBlockPos(context, ANCHOR_POS);
        GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        String name = StringArgumentType.getString(context, FILE_NAME);
        return execute(context.getSource(), from, to, Optional.of(anchorPos), profile, name);
    }

    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral(NAME)
                .then(newArgument(POS1, BlockPosArgument.blockPos())
                        .then(newArgument(POS2, BlockPosArgument.blockPos())
                                .executes(ScanCommand::onExecute)
                                .then(newArgument(ANCHOR_POS, BlockPosArgument.blockPos())
                                        .executes(ScanCommand::onExecuteWithAnchor))
                                .then(newArgument(PLAYER_NAME, GameProfileArgument.gameProfile())
                                        .executes(ScanCommand::onExecuteWithPlayerName)
                                        .then(newArgument(FILE_NAME, StringArgumentType.word())
                                                .executes(ScanCommand::onExecuteWithPlayerNameAndFileName)
                                                .then(newArgument(ANCHOR_POS, BlockPosArgument.blockPos())
                                                        .executes(ScanCommand::onExecuteWithPlayerNameAndFileNameAndAnchorPos))))));
    }
}
