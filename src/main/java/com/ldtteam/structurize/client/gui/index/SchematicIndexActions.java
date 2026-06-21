package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.structurize.api.constants.TranslationConstants;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.index.PackManager;
import com.ldtteam.structurize.index.models.Pack;
import com.ldtteam.structurize.index.models.PackSchematic;
import com.ldtteam.structurize.network.messages.index.*;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Shared action handlers for the schematic index GUI windows.
 *
 * <p>These actions can apply at pack scope, schematic group scope (all levels), or single-level
 * scope, and are reused across multiple windows rather than duplicated inline.
 */
class SchematicIndexActions
{
    private SchematicIndexActions() {}

    /**
     * Requests re-validation of all schematics within a pack.
     *
     * @param packId the pack identifier
     */
    static void validatePack(final String packId)
    {
        new ValidatePackMessage(packId).sendToServer();
    }

    /**
     * Saves (validates and scans) all schematics within a pack.
     *
     * @param packId the pack identifier
     */
    static void savePack(final String packId)
    {
        new SavePackMessage(packId).sendToServer();
    }

    /**
     * Deletes all schematics within a pack.
     *
     * @param packId the pack identifier
     */
    static void deletePack(final String packId)
    {
        final Pack pack = PackManager.getClientPack(packId);
        if (pack == null)
        {
            return;
        }

        new WindowConfirmDelete(Component.translatable(TranslationConstants.SCHEMATIC_INDEX_CONFIRM_DELETE_PACK, pack.name()),
            () -> new DeletePackMessage(packId).sendToServer()).openAsLayer();
    }

    /**
     * Requests re-validation of all levels of a schematic group within a pack.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     */
    static void validateSchematic(final String packId, final String path, final String name)
    {
        new ValidateSchematicMessage(packId, path, name).sendToServer();
    }

    /**
     * Saves (validates and scans) all levels of a schematic group within a pack.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     */
    static void saveSchematic(final String packId, final String path, final String name)
    {
        new SaveSchematicMessage(packId, path, name).sendToServer();
    }

    /**
     * Deletes all levels of a schematic group within a pack.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     */
    static void deleteSchematic(final String packId, final String path, final String name)
    {
        final Pack pack = PackManager.getClientPack(packId);
        if (pack == null)
        {
            return;
        }

        final String label = path.isEmpty() ? name : path + "/" + name;
        new WindowConfirmDelete(Component.translatable(TranslationConstants.SCHEMATIC_INDEX_CONFIRM_DELETE_SCHEMATIC, label, pack.name()),
            () -> new DeleteSchematicMessage(packId, path, name).sendToServer()).openAsLayer();
    }

    /**
     * Requests re-validation of a single level of a schematic within a pack.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     * @param level  the specific level to validate (1-based)
     */
    static void validateSchematic(final String packId, final String path, final String name, final int level)
    {
        new ValidateSchematicMessage(packId, path, name, level).sendToServer();
    }

    /**
     * Saves (validates and scans) a single level of a schematic within a pack.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     * @param level  the specific level to save (1-based)
     */
    static void saveSchematic(final String packId, final String path, final String name, final int level)
    {
        new SaveSchematicMessage(packId, path, name, level).sendToServer();
    }

    /**
     * Deletes a single level of a schematic within a pack.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     * @param level  the specific level to delete (1-based)
     */
    static void deleteSchematic(final String packId, final String path, final String name, final int level)
    {
        final Pack pack = PackManager.getClientPack(packId);
        if (pack == null)
        {
            return;
        }

        final String label = path.isEmpty() ? name : path + "/" + name;
        new WindowConfirmDelete(Component.translatable(TranslationConstants.SCHEMATIC_INDEX_CONFIRM_DELETE_LEVEL, level, label, pack.name()),
            () -> new DeleteSchematicMessage(packId, path, name, level).sendToServer()).openAsLayer();
    }

    /**
     * Highlights (renders the bounding box of) a single schematic level on the client.
     *
     * @param schematic the schematic level to highlight
     */
    static void highlightSchematicLevel(final PackSchematic schematic)
    {
        RenderingCache.queue("scan", new BoxPreviewData(schematic.pos1(), schematic.pos2(), schematic.anchor()));
    }

    /**
     * Teleports the player to a single schematic level.
     *
     * @param schematic the schematic level to teleport to
     */
    static void teleportToSchematicLevel(final PackSchematic schematic)
    {
        new TeleportToSchematicLevelMessage(schematic.pos1(), schematic.pos2()).sendToServer();
    }

    /**
     * Opens the placement GUI to relocate a single schematic level.
     *
     * <p>The blueprint is built client-side from the schematic's stored bounding box and loaded
     * into the placement preview. On confirmation, the blocks are placed at the chosen location and the
     * pack data is updated to match.
     *
     * @param packId the pack identifier
     * @param path   the relative folder path of the schematic (may be empty for root-level)
     * @param name   the schematic file name (without extension)
     * @param level  the specific level to relocate (1-based)
     */
    static void relocateSchematicLevel(final String packId, final String path, final String name, final int level)
    {
        final Pack pack = PackManager.getClientPack(packId);
        if (pack == null)
        {
            return;
        }

        final PackSchematic schematic = pack.schematics().stream().filter(s -> s.path().equals(path) && s.name().equals(name) && s.level() == level).findFirst().orElse(null);

        if (schematic == null)
        {
            return;
        }

        final BoundingBox box = BoundingBox.fromCorners(schematic.pos1(), schematic.pos2());
        final Blueprint blueprint = BlueprintUtil.createBlueprint(Minecraft.getInstance().level,
            new BlockPos(box.minX(), box.minY(), box.minZ()),
            (short) box.getXSpan(),
            (short) box.getYSpan(),
            (short) box.getZSpan(),
            schematic.getFullSchematicPath(),
            schematic.anchor());

        RenderingCache.getOrCreateBlueprintPreviewData(WindowSchematicRelocate.PREVIEW_KEY).setBlueprint(blueprint);
        new WindowSchematicRelocate(packId, path, name, level, Minecraft.getInstance().player.blockPosition()).open();
    }
}
