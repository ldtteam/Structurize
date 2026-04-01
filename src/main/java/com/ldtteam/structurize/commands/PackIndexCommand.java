// TODO: Remove before publication — debug command only
package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.index.packtypes.PackTypesRegistry;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
                    .then(newArgument("packName", StringArgumentType.word())
                        .executes(ctx -> addPack(ctx.getSource(), StringArgumentType.getString(ctx, "packName")))))
                .then(newLiteral("schematic")
                    .then(newArgument("packName", StringArgumentType.word())
                        .then(newArgument("schematicName", StringArgumentType.word())
                            .then(newArgument("path", StringArgumentType.word())
                                .then(newArgument("level", IntegerArgumentType.integer(0))
                                    .executes(ctx -> addSchematic(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "packName"),
                                        StringArgumentType.getString(ctx, "schematicName"),
                                        StringArgumentType.getString(ctx, "path"),
                                        IntegerArgumentType.getInteger(ctx, "level")))))))));
    }

    private static ServerLevel getOverworld(final CommandSourceStack source)
    {
        return source.getServer().getLevel(Level.OVERWORLD);
    }

    private static int addPack(final CommandSourceStack source, final String packName)
    {
        final ServerLevel overworld = getOverworld(source);
        if (overworld == null)
        {
            return 0;
        }

        final PackManager manager = overworld.getDataStorage().computeIfAbsent(PackManager.FACTORY, PackManager.DATA_NAME);
        final String newId = manager.addPack(packName, PackTypesRegistry.DEFAULT_PACK_TYPE);

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
        final String packName,
        final String schematicName,
        final String path,
        final int level)
    {
        final ServerLevel overworld = getOverworld(source);
        if (overworld == null)
        {
            return 0;
        }

        if (PackManager.getServerPacks(overworld).stream().noneMatch(p -> p.getName().equals(packName)))
        {
            source.sendFailure(net.minecraft.network.chat.Component.literal("Pack not found: " + packName));
            return 0;
        }

        final PackSchematic schematic = new PackSchematic(
            path,
            schematicName,
            level,
            BlockPos.ZERO,
            BlockPos.ZERO,
            null,
            source.getLevel().dimension());

        overworld.getDataStorage().computeIfAbsent(PackManager.FACTORY, PackManager.DATA_NAME)
            .addSchematic(packName, schematic);

        LOGGER.info("[PackIndex] Added schematic '{}' to pack '{}'", schematicName, packName);
        source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("Added schematic '" + schematicName + "' to pack '" + packName + "'"), false);
        return 1;
    }
}
