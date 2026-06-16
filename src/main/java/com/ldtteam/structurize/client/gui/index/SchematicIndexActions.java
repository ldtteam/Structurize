package com.ldtteam.structurize.client.gui.index;

import com.ldtteam.structurize.network.messages.SavePackMessage;
import com.ldtteam.structurize.network.messages.SaveSchematicMessage;
import com.ldtteam.structurize.network.messages.ValidatePackMessage;
import com.ldtteam.structurize.network.messages.ValidateSchematicMessage;

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
        // TODO
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
        // TODO
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
        // TODO
    }
}