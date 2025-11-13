package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.IOPool;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.ldtteam.structurize.blueprints.v1.BlueprintUtil.DEFAULT_FIXER_IF_NOT_FOUND;

/**
 * Command for updating schematic files to the current minecraft version(datafixer)
 */
public class UpdateSchematicPackCommand extends AbstractCommand
{
    /**
     * Descriptive string.
     */
    public final static String commandName = "updateSchematicPack";

    /**
     * The pack name command argument.
     */
    private static final String PACK_NAME = "pack";

    private static int execute(CommandContext<CommandSourceStack> context)
    {
        final String packName = StringArgumentType.getString(context, PACK_NAME);
        final StructurePackMeta packMeta = StructurePacks.getStructurePack(packName);

        if (packMeta == null)
        {
            context.getSource().sendSystemMessage(Component.literal("Failed to find pack: " + packName));
            return 0;
        }

        fixBlueprints(packMeta, context.getSource());
        return 1;
    }

    /**
     * Loads and saves blueprints which got saved on a previous minecraft version
     *
     * @param packMeta
     * @param source
     */
    public static void fixBlueprints(final StructurePackMeta packMeta, final CommandSourceStack source)
    {
        CompletableFuture.supplyAsync(() -> {
            try (final Stream<Path> paths = Files.walk(packMeta.getPath()))
            {
                paths.filter(Files::isRegularFile).filter(file -> file.toString().endsWith(".blueprint")).forEach(file ->
                {
                    try
                    {
                        final ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(file));
                        final CompoundTag nbt = NbtIo.readCompressed(inputStream);
                        inputStream.close();

                        int currentDataVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
                        final int oldDataVersion = nbt.contains("mcversion") ? nbt.getInt("mcversion") : DEFAULT_FIXER_IF_NOT_FOUND;

                        if (oldDataVersion != currentDataVersion)
                        {
                            final Blueprint blueprint = BlueprintUtil.readBlueprintFromNBT(nbt);
                            if (blueprint == null)
                            {
                                return;
                            }

                            final CompoundTag newNbt = BlueprintUtil.writeBlueprintToNBT(blueprint);
                            final Path schematicPath = file.subpath(2, file.getNameCount());

                            var output = Minecraft.getInstance().gameDirectory.toPath()
                                .resolve("blueprints")
                                .resolve(schematicPath.toString());

                            output.getParent().toFile().mkdirs();
                            output.toFile().createNewFile();

                            try (final OutputStream outputstream = new BufferedOutputStream(Files.newOutputStream(output)))
                            {
                                NbtIo.writeCompressed(newNbt, outputstream);
                            }
                            catch (final IOException e)
                            {
                                source.sendSystemMessage(Component.literal("Error saving blueprint:  " + packMeta.getName() + ":" + file + e));
                                return;
                            }

                            source.sendSystemMessage(Component.literal("Updated blueprint: " + schematicPath));
                        }
                    }
                    catch (final IOException e)
                    {
                        source.sendSystemMessage(Component.literal("Error loading blueprint: " + packMeta.getName() + ":" + file + e));
                    }
                });
            }
            catch (Exception e)
            {
                source.sendSystemMessage(Component.literal("Error loading folder: " + packMeta.getName() + e));
            }

            return null;
        }, IOPool.getExecutor()).whenComplete((a, b) ->
            source.sendSystemMessage(Component.literal("Finished updating schematics in pack: " + packMeta.getName())));
    }

    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(commandName)
            .then(newArgument(PACK_NAME, StringArgumentType.greedyString()).suggests((SuggestionProvider) (context, builder) -> {
                for (final var packMeta : StructurePacks.getPackMetas())
                {
                    builder.suggest(packMeta.getName());
                }
                return builder.buildFuture();
            }).executes(UpdateSchematicPackCommand::execute));
    }
}
