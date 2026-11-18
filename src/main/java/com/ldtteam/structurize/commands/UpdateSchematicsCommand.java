package com.ldtteam.structurize.commands;

import com.ldtteam.structurize.api.BlockPosUtil;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.blueprints.conversion.StructurePackImporter;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.DataFixerUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.apache.logging.log4j.LogManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.ldtteam.structurize.api.constants.Constants.*;
import static com.ldtteam.structurize.blueprints.v1.BlueprintUtil.*;

/**
 * Command to update all schematics in structurize/updater/input to the blueprint format to structurize/updater/output.
 */
public class UpdateSchematicsCommand extends AbstractCommand
{
    private final static String NAME = "updateschematics";

    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return newLiteral(NAME).executes(s -> onExecute(s));
    }

    private static int onExecute(final CommandContext<CommandSourceStack> command) throws CommandSyntaxException
    {
        final Path gameFolder = new File(".").toPath().resolve(BLUEPRINT_FOLDER).resolve(UPDATE_FOLDER);
        try
        {
            Files.createDirectories(gameFolder);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            try (final Stream<Path> paths = Files.list(gameFolder.resolve("input")))
            {
                paths.forEach(element -> update(element, gameFolder.resolve("input"), gameFolder.resolve("output"), command.getSource().registryAccess()));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        return 1;
    }

    private static void update(final Path input, final Path globalInputFolder, final Path globalOutputFolder, final HolderLookup.Provider provider)
    {
        if (Files.isDirectory(input))
        {
            try (final Stream<Path> paths = Files.list(input))
            {
                paths.forEach(element -> update(element, globalInputFolder, globalOutputFolder, provider));
            }
            catch (final IOException e)
            {
                Log.getLogger().warn("Could not read schematic folder: " + input, e);
            }
            return;
        }

        final String fileName = input.getFileName().toString();
        if (!StructurePackImporter.isSupported(input) && !fileName.endsWith(".blueprint"))
        {
            return;
        }
        try
        {
            final Path relative = globalInputFolder.relativize(input);
            final String outputName = StructurePackImporter.blueprintName(input) + ".blueprint";
            if (!fileName.endsWith(".blueprint") && Files.exists(input.resolveSibling(outputName)))
            {
                return;
            }
            final Path output = globalOutputFolder.resolve(relative).resolveSibling(outputName);
            Files.createDirectories(output.getParent());

            final CompoundTag blueprint;
            if (fileName.endsWith(".blueprint"))
            {
                final Blueprint existing = fixBluePrints(input, provider);
                if (existing == null)
                {
                    return;
                }
                blueprint = writeBlueprintToNBT(existing);
            }
            else
            {
                blueprint = StructurePackImporter.convert(input);
            }
            NbtIo.writeCompressed(blueprint, output);
        }
        catch (final IOException | RuntimeException e)
        {
            Log.getLogger().warn("Could not update schematic: " + input, e);
        }
    }

    private static Blueprint fixBluePrints(final Path input, final HolderLookup.Provider provider)
    {
        try
        {
            final CompoundTag compoundNBT = NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(input)), NbtAccounter.unlimitedHeap());
            return readBlueprintFromNBT(compoundNBT, provider);
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Could not read file:" + input.toString());
        }
        return null;
    }

    public static Blueprint readBlueprintFromNBT(final CompoundTag nbtTag, final HolderLookup.Provider provider)
    {
        final CompoundTag tag = nbtTag;
        byte version = tag.getByte("version");
        if (version == 1)
        {
            short sizeX = tag.getShort("size_x"), sizeY = tag.getShort("size_y"), sizeZ = tag.getShort("size_z");

            // Reading required Mods
            List<String> requiredMods = new ArrayList<>();
            List<String> missingMods = new ArrayList<>();
            ListTag modsList = (ListTag) tag.get("required_mods");
            short modListSize = (short) modsList.size();
            for (int i = 0; i < modListSize; i++)
            {
                requiredMods.add((modsList.get(i)).getAsString());
                if (!requiredMods.get(i).equals("minecraft") && !ModList.get().getModContainerById(requiredMods.get(i)).isPresent())
                {
                    LogManager.getLogger().warn("Found missing mods for Blueprint, some blocks may be missing: " + requiredMods.get(i));
                    missingMods.add(requiredMods.get(i));
                }
            }

            final int oldDataVersion = tag.contains("mcversion") ? tag.getInt("mcversion") : DEFAULT_FIXER_IF_NOT_FOUND;

            // Reading Pallete
            ListTag paletteTag = (ListTag) tag.get("palette");
            List<BlockState> palette = new ArrayList<>();

            // Reading Blocks
            short[][][] blocks = convertSaveDataToBlocks(tag.getIntArray("blocks"), sizeX, sizeY, sizeZ);

            // Reading Tile Entities
            CompoundTag[] tes = fixTileEntities(oldDataVersion, (ListTag) tag.get("tile_entities"));

            final List<CompoundTag> teList = new ArrayList<>();

            UpdateSchematicsCommand.fixPalette(oldDataVersion, palette, teList, paletteTag, blocks, new BlockPos(sizeX, sizeY, sizeZ));

            teList.addAll(Arrays.stream(tes).toList());

            final CompoundTag[] tileEntities = teList.toArray(new CompoundTag[0]);


            // Reading Entities
            CompoundTag[] entities = fixEntities(oldDataVersion, (ListTag) tag.get("entities"));

            if (oldDataVersion == DEFAULT_FIXER_IF_NOT_FOUND)
            {
                fixCross1343(palette, blocks, tileEntities, entities);
            }

            final Blueprint schem = new Blueprint(sizeX, sizeY, sizeZ, (short) palette.size(), palette, blocks, tileEntities, requiredMods, provider)
                                      .setMissingMods(missingMods.toArray(new String[0]));

            schem.setEntities(entities);

            if (tag.getAllKeys().contains("name"))
            {
                schem.setName(tag.getString("name"));
            }
            if (tag.getAllKeys().contains("architects"))
            {
                ListTag architectsTag = (ListTag) tag.get("architects");
                String[] architects = new String[architectsTag.size()];
                for (int i = 0; i < architectsTag.size(); i++)
                {
                    architects[i] = architectsTag.getString(i);
                }
                schem.setArchitects(architects);
            }

            if (tag.getAllKeys().contains(NBT_OPTIONAL_DATA_TAG))
            {
                final CompoundTag optionalTag = tag.getCompound(NBT_OPTIONAL_DATA_TAG);
                if (optionalTag.getAllKeys().contains(MOD_ID))
                {
                    final CompoundTag structurizeTag = optionalTag.getCompound(MOD_ID);
                    BlockPos offsetPos = BlockPosUtil.readFromNBT(structurizeTag, "primary_offset");
                    schem.setCachePrimaryOffset(offsetPos);
                }
            }

            return schem;
        }
        return null;
    }

    public static void fixPalette(
      final int oldDataVersion,
      final List<BlockState> palette,
      final List<CompoundTag> tileEntities,
      final ListTag paletteTag, final short[][][] blocks, final BlockPos blockPos)
    {
        final short paletteSize = (short) paletteTag.size();

        for (short i = 0; i < paletteSize; i++)
        {
            final CompoundTag nbt = paletteTag.getCompound(i);
            try
            {
                final CompoundTag fixedNbt = DataFixerUtils.runDataFixer(nbt, References.BLOCK_STATE, oldDataVersion);
                final String name = fixedNbt.getString("Name");
                if (!name.startsWith("%s:".formatted(MOD_ID)))
                {
                    final BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), fixedNbt);
                    palette.add(i, state);
                    continue;
                }

                final BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), fixedNbt);
                palette.add(i, state);
            }
            catch (final Exception e)
            {
                palette.add(i, Blocks.AIR.defaultBlockState());
                Log.getLogger().warn("Blueprint reader: something went wrong loading block at position: " + i, e);
            }
        }
    }
}
