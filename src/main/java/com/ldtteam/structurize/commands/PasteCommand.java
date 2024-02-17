package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.blocks.interfaces.ISpecialCreativeHandlerAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.util.TickedWorldOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.command.EnumArgument;
import org.jetbrains.annotations.Nullable;

/**
 * Command for opening Pasting a structure from a file
 */
public class PasteCommand extends AbstractCommand
{
    /**
     * Descriptive string.
     */
    public final static String commandName = "paste";

    /**
     * The player not found for a scan 
     */
    private static final String PLAYER_NOT_FOUND = "com.structurize.command.playernotfound";

    /**
     * The scan success 
     */
    private static final String PASTE_SUCCESS_MESSAGE = "com.structurize.command.paste.success";

    /**
     * The no permission message reply.
     */
    private static final String NO_PERMISSION_MESSAGE = "com.structurize.command.paste.no.perm";

    /**
     * The invalid pack reply.
     */
    private static final String NO_PACK_MESSAGE = "com.structurize.command.paste.no.pack";

    /**
     * The invalid pack reply.
     */
    private static final String NO_BLUEPRINT_MESSAGE = "com.structurize.command.paste.no.blueprint";

    /**
     * The player name command argument.
     */
    private static final String PLAYER_NAME = "player";

    /**
     * Position command argument.
     */
    private static final String POS = "pos";

    /**
     * The pack name command argument.
     */
    private static final String PACK_NAME = "pack";

    /**
     * The filename command argument.
     */
    private static final String FILE_PATH = "path";

    /**
     * The rotation command argument.
     */
    private static final String ROT_MIR = "rotation_mirror";

    /**
     * The pretty command argument.
     */
    private static final String PRETTY = "pretty";

    private static int execute(final CommandSourceStack source, final BlockPos pos, final String pack, final String tempPath, final RotationMirror rotMir, final boolean pretty, final Player player) throws CommandSyntaxException
    {
        @Nullable final Level world = source.getLevel();
        if (source.getEntity() instanceof Player && !source.getPlayerOrException().isCreative())
        {
            source.sendFailure(Component.literal(NO_PERMISSION_MESSAGE));
            return 0;
        }

        if (!(player instanceof ServerPlayer))
        {
            source.sendFailure(Component.translatable(PLAYER_NOT_FOUND));
            return 0;
        }

        final String[] split = tempPath.split("\\.");
        final StringBuilder builder = new StringBuilder();
        for (final String part : split)
        {
            if (!builder.toString().isEmpty())
            {
                builder.append("/");
            }
            builder.append(part);
        }
        final String path = builder.toString();

        final String[] packSplit = pack.split("\\.");
        final StringBuilder packBuilder = new StringBuilder();
        for (final String part : packSplit)
        {
            if (!packBuilder.toString().isEmpty())
            {
                packBuilder.append(" ");
            }
            packBuilder.append(part);
        }
        final String packName = packBuilder.toString();

        if (!StructurePacks.hasPack(packName))
        {
            source.sendFailure(Component.translatable(NO_PACK_MESSAGE));
            return 0;
        }

        final Blueprint blueprint = StructurePacks.getBlueprint(packName, path + ".blueprint", true);
        if (blueprint == null)
        {
            source.sendFailure(Component.translatable(NO_BLUEPRINT_MESSAGE));
            return 0;
        }

        final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        blueprint.setRotationMirror(rotMir, world);

        final IStructureHandler structure;
        if (anchor.getBlock() instanceof final ISpecialCreativeHandlerAnchorBlock specialAnchor)
        {
            if (!specialAnchor.setup((ServerPlayer) player, world, pos, blueprint, rotMir, pretty, packName, path))
            {
                return 0;
            }
            structure = specialAnchor.getStructureHandler(world, pos, blueprint, rotMir, pretty);
        }
        else
        {
            structure = new CreativeStructureHandler(world, pos, blueprint, rotMir, pretty);
        }

        final StructurePlacer instantPlacer = new StructurePlacer(structure);
        Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));

        source.sendSuccess(() -> Component.translatable(PASTE_SUCCESS_MESSAGE), true);
        return 1;
    }

    private static int onExecute(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);

        return execute(context.getSource(), pos, packName, path, RotationMirror.NONE, true, context.getSource().getPlayer());
    }

    private static int onExecuteWithRotationAndMirror(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);
        final RotationMirror rotMir = context.getArgument(ROT_MIR, RotationMirror.class);

        return execute(context.getSource(), pos, packName, path, rotMir, true, context.getSource().getPlayer());
    }

    private static int onExecuteWithFull(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);
        final RotationMirror rotMir = context.getArgument(ROT_MIR, RotationMirror.class);
        final boolean pretty = BoolArgumentType.getBool(context, PRETTY);

        return execute(context.getSource(), pos, packName, path, rotMir, pretty, context.getSource().getPlayer());
    }

    private static int onExecuteWithFullAndPlayer(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);
        final RotationMirror rotMir = context.getArgument(ROT_MIR, RotationMirror.class);
        final boolean pretty = BoolArgumentType.getBool(context, PRETTY);
        final GameProfile profile = GameProfileArgument.getGameProfiles(context, PLAYER_NAME).stream().findFirst().orElse(null);

        if (profile == null)
        {
            context.getSource().sendFailure(Component.translatable(PLAYER_NOT_FOUND));
            return 0;
        }

        return execute(context.getSource(), pos, packName, path, rotMir, pretty, context.getSource().getLevel().getServer().getPlayerList().getPlayer(profile.getId()));
    }

    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(commandName)
          .then(newArgument(POS, BlockPosArgument.blockPos())
            .then(newArgument(PACK_NAME, StringArgumentType.string())
              .then(newArgument(FILE_PATH, StringArgumentType.string())
                .executes(PasteCommand::onExecute)
                .then(newArgument(ROT_MIR, EnumArgument.enumArgument(RotationMirror.class))
                  .executes(PasteCommand::onExecuteWithRotationAndMirror)
                      .then(newArgument(PRETTY, BoolArgumentType.bool())
                        .executes(PasteCommand::onExecuteWithFull)
                            .then(newArgument(PLAYER_NAME, GameProfileArgument.gameProfile())
                                    .executes(PasteCommand::onExecuteWithFullAndPlayer)))))));
    }
}
