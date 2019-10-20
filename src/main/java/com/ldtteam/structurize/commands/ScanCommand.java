package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.items.ItemScanTool;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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
     * Execute the scan command code.
     *
     * @param context the command context.
     * @return 1 if successful, 0 if unsuccessful.
     *
     * @throws CommandSyntaxException possible issues.
     */
    private static int onExecute(final CommandContext<CommandSource> context) throws CommandSyntaxException
    {
        final BlockPos from = BlockPosArgument.getBlockPos(context, POS1);
        final BlockPos to = BlockPosArgument.getBlockPos(context, POS2);
        final GameProfile profile;
        if (context.getNodes().size() > 4)
        {
            profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);
        }
        else
        {
            profile = null;
        }

        @Nullable final World world = context.getSource().getWorld();
        if (context.getSource().getEntity() instanceof PlayerEntity && !context.getSource().asPlayer().isCreative())
        {
            context.getSource().sendErrorMessage(new StringTextComponent(NO_PERMISSION_MESSAGE));
        }

        final PlayerEntity player;
        if (profile != null)
        {
            player = world.getServer().getPlayerList().getPlayerByUUID(profile.getId());
            if (player == null)
            {
                context.getSource().sendErrorMessage(new TranslationTextComponent(PLAYER_NOT_FOUND, profile.getName()));
                return 0;
            }
        }
        else if (context.getSource().getEntity() instanceof PlayerEntity)
        {
            player = context.getSource().asPlayer();
        }
        else
        {
            context.getSource().sendErrorMessage(new TranslationTextComponent(PLAYER_NOT_FOUND, profile.getName()));
            return 0;
        }

        final String name;
        if (context.getNodes().size() > 5)
        {
            name = StringArgumentType.getString(context, FILE_NAME);
        }
        else
        {
            name = null;
        }

        ItemScanTool.saveStructure(world, from, to, player, name == null ? "" : name);
        context.getSource().sendErrorMessage(new TranslationTextComponent(SCAN_SUCCESS_MESSAGE));
        return 1;
    }

    protected static LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral(NAME)
                 .then(newArgument(POS1, BlockPosArgument.blockPos())
                         .then(newArgument(POS2, BlockPosArgument.blockPos())
                                 .executes(ScanCommand::onExecute)
                                 .then(newArgument(PLAYER_NAME, GameProfileArgument.gameProfile())
                                         .executes(ScanCommand::onExecute)
                                         .then(newArgument(FILE_NAME, StringArgumentType.word())
                                                 .executes(ScanCommand::onExecute)))));
    }
}
