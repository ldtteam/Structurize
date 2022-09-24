package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.blocks.interfaces.ILeveledBlueprintAnchorBlock;
import com.ldtteam.structurize.blocks.interfaces.ISpecialCreativeHandlerAnchorBlock;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for opening Pasting a structure from a file
 */
public class PasteFolderCommand extends AbstractCommand
{
    /**
     * Descriptive string.
     */
    public final static String commandName = "pasteFolder";

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
    private static final String ROTATION = "rotation";

    /**
     * The mirror command argument.
     */
    private static final String MIRROR = "mirror";

    /**
     * The pretty command argument.
     */
    private static final String PRETTY = "pretty";

    private static int execute(final CommandSourceStack source, final BlockPos pos, final String pack, final String tempPath, final Rotation rotation, final boolean mirrored, final boolean pretty) throws CommandSyntaxException
    {
        @Nullable final Level world = source.getLevel();
        if (source.getEntity() instanceof Player && !source.getPlayerOrException().isCreative())
        {
            source.sendFailure(Component.literal(NO_PERMISSION_MESSAGE));
        }

        final Player player;
        if (source.getEntity() instanceof Player)
        {
            player = source.getPlayerOrException();
        } 
        else
        {
            source.sendFailure(Component.translatable(PLAYER_NOT_FOUND));
            return 0;
        }

        final Mirror mirror = mirrored ? Mirror.FRONT_BACK : Mirror.NONE;

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

        ServerFutureProcessor.queueBlueprintList(new ServerFutureProcessor.BlueprintListProcessingData(StructurePacks.getBlueprintsFuture(packName, path), world, (list) -> {

            final Map<String, List<Blueprint>> blueprintMapping = new LinkedHashMap<>();

            // todo bulk up the ones with the same special in the GUI (those go on the same x) and the others then on the same (z)
            for (Blueprint blueprint : list)
            {
                final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                if (anchor.getBlock() instanceof ILeveledBlueprintAnchorBlock)
                {
                    final int level = ((ILeveledBlueprintAnchorBlock) anchor.getBlock()).
                      getLevel(blueprint.getTileEntityData(BlockPos.ZERO, blueprint.getPrimaryBlockOffset()));
                    final String name = blueprint.getFileName().replace(Integer.toString(level), "");
                    final List<Blueprint> blueprintList = blueprintMapping.getOrDefault(name, new ArrayList<>());
                    blueprintList.add(blueprint);
                    blueprintMapping.put(name, blueprintList);
                }
                else
                {
                    final String name = blueprint.getFileName();
                    final List<Blueprint> blueprintList = blueprintMapping.getOrDefault(name, new ArrayList<>());
                    blueprintList.add(blueprint);
                    blueprintMapping.put(name, blueprintList);
                }
            }

            int xOffset = 0;
            int zOffset = 0;
            for (List<Blueprint> perTypeList : blueprintMapping.values())
            {
                for (final Blueprint blueprint : perTypeList)
                {
                    final BlockState anchor = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                    blueprint.rotateWithMirror(rotation, mirror, world);
                    final BlockPos placementPos = pos.offset(xOffset, 0, zOffset).offset(blueprint.getPrimaryBlockOffset());

                    final IStructureHandler structure;
                    if (anchor.getBlock() instanceof ISpecialCreativeHandlerAnchorBlock)
                    {
                        if (!((ISpecialCreativeHandlerAnchorBlock) anchor.getBlock()).setup((ServerPlayer) player, world, placementPos, blueprint, new PlacementSettings(mirror, rotation),
                          pretty, packName, path))
                        {
                            return;
                        }
                        structure =
                          ((ISpecialCreativeHandlerAnchorBlock) anchor.getBlock()).getStructureHandler(world, placementPos, blueprint, new PlacementSettings(mirror, rotation),
                            pretty);
                    }
                    else
                    {
                        structure = new CreativeStructureHandler(world,
                          placementPos,
                          blueprint,
                          new PlacementSettings(mirror, rotation),
                          pretty);
                    }

                    final StructurePlacer instantPlacer = new StructurePlacer(structure);
                    Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
                    zOffset+=32;
                }
                xOffset+=32;
                zOffset = 0;
            }
        }));

        source.sendSuccess(Component.translatable(PASTE_SUCCESS_MESSAGE), true);
        return 1;
    }

    private static int onExecute(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);

        return execute(context.getSource(), pos, packName, path, Rotation.NONE, false, true);
    }

    private static int onExecuteWithRotation(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);
        final Rotation rotation = Rotation.values()[IntegerArgumentType.getInteger(context, ROTATION)];

        return execute(context.getSource(), pos, packName, path, rotation, false, true);
    }

    private static int onExecuteWithRotationAndMirror(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);
        final Rotation rotation = Rotation.values()[IntegerArgumentType.getInteger(context, ROTATION)];
        final boolean mirror = BoolArgumentType.getBool(context, MIRROR);

        return execute(context.getSource(), pos, packName, path, rotation, mirror, true);
    }

    private static int onExecuteWithFull(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final BlockPos pos = BlockPosArgument.getSpawnablePos(context, POS);
        final String packName  = StringArgumentType.getString(context, PACK_NAME);
        final String path  = StringArgumentType.getString(context, FILE_PATH);
        final Rotation rotation = Rotation.values()[IntegerArgumentType.getInteger(context, ROTATION)];
        final boolean mirror = BoolArgumentType.getBool(context, MIRROR);
        final boolean pretty = BoolArgumentType.getBool(context, PRETTY);

        return execute(context.getSource(), pos, packName, path, rotation, mirror, pretty);
    }

    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(commandName)
          .then(newArgument(POS, BlockPosArgument.blockPos())
            .then(newArgument(PACK_NAME, StringArgumentType.word())
              .then(newArgument(FILE_PATH, StringArgumentType.word())
                .executes(PasteFolderCommand::onExecute)
                .then(newArgument(ROTATION, IntegerArgumentType.integer(0, 3))
                  .executes(PasteFolderCommand::onExecuteWithRotation)
                  .then(newArgument(PLAYER_NAME, GameProfileArgument.gameProfile())
                    .then(newArgument(MIRROR, BoolArgumentType.bool())
                      .executes(PasteFolderCommand::onExecuteWithRotationAndMirror)
                      .then(newArgument(PRETTY, BoolArgumentType.bool())
                        .executes(PasteFolderCommand::onExecuteWithFull))))))));
    }
}
