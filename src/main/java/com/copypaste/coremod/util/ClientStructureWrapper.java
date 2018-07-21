package com.copypaste.coremod.util;

import com.copypaste.api.util.LanguageHandler;
import com.copypaste.api.util.Log;
import com.copypaste.api.util.Utils;
import com.copypaste.coremod.management.StructureName;
import com.copypaste.coremod.management.Structures;
import com.copypaste.structures.helpers.Settings;
import com.copypaste.structures.helpers.Structure;
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
        LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.copypaste.coremod.network.messages.schematicsavemessage.toobig", maxSize);
    }
}
