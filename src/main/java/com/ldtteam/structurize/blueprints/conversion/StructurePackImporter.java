package com.ldtteam.structurize.blueprints.conversion;

import com.ldtteam.structurize.api.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Prepares foreign schematic files before a writable structure pack is registered.
 * Only creates missing siblings; existing blueprints always take precedence.
 */
public final class StructurePackImporter
{
    private static final long MAX_NBT_SIZE = 64L * 1024 * 1024;

    private StructurePackImporter()
    {
    }

    /**
     * Import supported foreign structures recursively, keeping sources and category paths intact.
     * Runs synchronously on the discovery worker so registration cannot race pending conversions.
     * One invalid file must not prevent the rest of the pack from loading.
     *
     * @param packRoot the root of a writable structure pack.
     */
    public static void importStructures(final Path packRoot)
    {
        if (!Files.isWritable(packRoot))
        {
            return;
        }
        try (final Stream<Path> paths = Files.walk(packRoot))
        {
            paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                .filter(StructurePackImporter::isSupported)
                // Stable precedence when several source formats share the same blueprint name.
                .sorted(Comparator.comparing(Path::toString))
                .forEach(source ->
                {
                    try
                    {
                        importStructure(source);
                    }
                    catch (final Exception e)
                    {
                        Log.getLogger().warn("Unable to import schematic: " + source, e);
                    }
                });
        }
        catch (final IOException | UncheckedIOException e)
        {
            Log.getLogger().warn("Unable to discover schematics in pack: " + packRoot, e);
        }
    }

    /**
     * Compatibility entry point for callers of the initial vanilla-only importer.
     *
     * @param packRoot the writable pack root.
     */
    public static void importVanillaStructures(final Path packRoot)
    {
        importStructures(packRoot);
    }

    /**
     * @param source a potential schematic path.
     * @return whether an importer is available for its extension.
     */
    public static boolean isSupported(final Path source)
    {
        return switch (extension(source))
        {
            case ".nbt", ".schem", ".schematic", ".litematic" -> true;
            default -> false;
        };
    }

    /**
     * @param source the schematic path.
     * @return its filename without the format extension.
     */
    public static String blueprintName(final Path source)
    {
        final String fileName = source.getFileName().toString();
        final int separator = fileName.lastIndexOf('.');
        return separator == -1 ? fileName : fileName.substring(0, separator);
    }

    /**
     * Read bounded compressed NBT and dispatch to the converter for this source format.
     *
     * @param source the schematic path.
     * @return blueprint v1 NBT.
     * @throws IOException              if reading fails.
     * @throws IllegalArgumentException if the format or contents are unsupported.
     */
    public static CompoundTag convert(final Path source) throws IOException
    {
        if (!isSupported(source))
        {
            throw new IllegalArgumentException("Unsupported schematic extension: " + source);
        }
        final CompoundTag input = NbtIo.readCompressed(source, NbtAccounter.create(MAX_NBT_SIZE));
        final String name = blueprintName(source);
        return switch (extension(source))
        {
            case ".nbt" -> VanillaStructureImporter.convert(input, name);
            case ".schem" -> SpongeSchematicImporter.convert(input, name);
            case ".schematic" -> LegacySchematicImporter.convert(input, name);
            case ".litematic" -> LitematicaSchematicImporter.convert(input, name);
            default -> throw new IllegalArgumentException("Unsupported schematic extension: " + source);
        };
    }

    private static String extension(final Path source)
    {
        final String fileName = source.getFileName().toString();
        final int separator = fileName.lastIndexOf('.');
        return separator == -1 ? "" : fileName.substring(separator).toLowerCase(Locale.ROOT);
    }

    private static void importStructure(final Path source) throws IOException
    {
        final String name = blueprintName(source);
        final Path output = source.resolveSibling(name + ".blueprint");
        if (Files.exists(output, LinkOption.NOFOLLOW_LINKS))
        {
            return;
        }

        final CompoundTag blueprint = convert(source);
        final Path temporary = Files.createTempFile(source.getParent(), ".structurize-import-", ".tmp");
        try
        {
            NbtIo.writeCompressed(blueprint, temporary);
            // No REPLACE_EXISTING: a blueprint created while importing must also win.
            Files.move(temporary, output);
            Log.getLogger().info("Imported schematic: {}", source);
        }
        catch (final FileAlreadyExistsException e)
        {
            // Another discovery pass or the user supplied this blueprint in the meantime.
        }
        finally
        {
            Files.deleteIfExists(temporary);
        }
    }
}
