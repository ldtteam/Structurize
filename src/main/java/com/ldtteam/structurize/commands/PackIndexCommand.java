// TODO: Remove before publication — debug command only
package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.models.PackSchematicValidationState;
import com.ldtteam.structurize.index.packtypes.PackTypesRegistry;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PackIndexCommand extends AbstractCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String NAME = "packindex";

    protected static CommandSelection getEnvironmentType()
    {
        return CommandSelection.ALL;
    }

    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(NAME)
            .then(newLiteral("add")
                .then(newLiteral("pack")
                    .then(newArgument("packName", StringArgumentType.string())
                        .executes(ctx -> addPack(ctx.getSource(), StringArgumentType.getString(ctx, "packName"), ctx.getSource().getLevel()))))
                .then(newLiteral("schematic")
                    .then(newArgument("packId", StringArgumentType.string())
                        .then(newArgument("schematicName", StringArgumentType.string())
                            .then(newArgument("path", StringArgumentType.string())
                                .then(newArgument("level", IntegerArgumentType.integer(0))
                                    .executes(ctx -> addSchematic(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "packId"),
                                        StringArgumentType.getString(ctx, "schematicName"),
                                        StringArgumentType.getString(ctx, "path"),
                                        IntegerArgumentType.getInteger(ctx, "level")))))))));
    }

    private static int addPack(final CommandSourceStack source, final String packName, final ServerLevel level)
    {
        final String newId = PackManager.addPack(packName, PackTypesRegistry.DEFAULT_PACK_TYPE, level);

        if (newId != null)
        {
            LOGGER.info("[PackIndex] Added pack '{}'", packName);
            source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Added pack: " + packName), false);
        }
        else
        {
            LOGGER.info("[PackIndex] Pack not added: '{}', duplicate name", packName);
            source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Couldn't add pack: " + packName + ", duplicate name"), false);
        }
        return 1;
    }

    private static int addSchematic(
        final CommandSourceStack source,
        final String packId,
        final String schematicName,
        final String path,
        final int level)
    {
        if (PackManager.getPack(packId) == null)
        {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Pack not found: " + packId));
            return 0;
        }

        final PackSchematic schematic = new PackSchematic(path, schematicName, level, BlockPos.ZERO, BlockPos.ZERO, null, new PackSchematicValidationState());
        PackManager.addSchematic(packId, schematic);

        LOGGER.info("[PackIndex] Added schematic '{}' to pack '{}'", schematicName, packId);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Added schematic '" + schematicName + "' to pack '" + packId + "'"), false);
        return 1;
    }
}
