package com.structurize.coremod.util;

import com.structurize.api.util.LanguageHandler;
import com.structurize.api.util.Log;
import com.structurize.api.util.Utils;
import com.structurize.coremod.management.StructureName;
import com.structurize.coremod.management.Structures;
import com.structurize.structures.helpers.Settings;
import com.structurize.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

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
     * @param nbttagcompound compound to store.
     * @param fileName  milli seconds for fileName.
     */
    public static void handleSaveScanMessage(final NBTTagCompound nbttagcompound, final String fileName)
    {
        final StructureName structureName =
          new StructureName(Structures.SCHEMATICS_SCAN, "new", fileName);
        final File file = new File(Structure.getClientSchematicsFolder(), structureName.toString() + Structures.SCHEMATIC_EXTENSION);
        Utils.checkDirectory(file.getParentFile());

        try (OutputStream outputstream = new FileOutputStream(file))
        {
            CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
        }
        catch (final IOException e)
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "item.scepterSteel.scanFailure");
            Log.getLogger().warn("Exception while trying to scan.", e);
            return;
        }

        LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "item.scepterSteel.scanSuccess", file);
        Settings.instance.setStructureName(structureName.toString());
    }

    /**
     * Send a message to the player informing him that the schematic is too big.
     *
     * @param maxSize is the maximum size allowed in bytes.
     */
    public static void sendMessageSchematicTooBig(final int maxSize)
    {
        LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.structurize.coremod.network.messages.schematicsavemessage.toobig", maxSize);
    }
}
