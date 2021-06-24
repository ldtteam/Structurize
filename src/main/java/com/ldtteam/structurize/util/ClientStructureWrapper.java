package com.ldtteam.structurize.util;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Client only structure wrapper methods.
 */
public final class ClientStructureWrapper
{
    /**
     * Private constructor to hide implicit one.
     */
    private ClientStructureWrapper()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Handles the save message of scans.
     *
     * @param CompoundNBT compound to store.
     * @param fileName       milli seconds for fileName.
     */
    public static void handleSaveScanMessage(final CompoundNBT CompoundNBT, final String fileName)
    {
        final StructureName structureName =
          new StructureName(Structures.SCHEMATICS_SCAN, "new", fileName);

        final File file = new File(new File(Minecraft.getInstance().gameDirectory, Constants.MOD_ID), structureName.toString() + Structures.SCHEMATIC_EXTENSION_NEW);
        Utils.checkDirectory(file.getParentFile());

        try (final OutputStream outputstream = new FileOutputStream(file))
        {
            CompressedStreamTools.writeCompressed(CompoundNBT, outputstream);
        }
        catch (final IOException e)
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "item.scepterSteel.scanFailure");
            Log.getLogger().warn("Exception while trying to scan.", e);
            return;
        }

        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "item.scepterSteel.scanSuccess", file);
        Settings.instance.setStructureName(structureName.toString());
    }

    /**
     * Send a message to the player informing him that the schematic is too big.
     *
     * @param maxSize is the maximum size allowed in bytes.
     */
    public static void sendMessageSchematicTooBig(final int maxSize)
    {
        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "com.ldtteam.structurize.network.messages.schematicsavemessage.toobig", maxSize);
    }
}
