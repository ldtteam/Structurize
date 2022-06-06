package com.ldtteam.structurize.util;

import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.Utils;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

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
    @SuppressWarnings("resource")
    public static void handleSaveScanMessage(final CompoundTag CompoundNBT, final String fileName)
    {
        final StructureName structureName =
          new StructureName(Structures.SCHEMATICS_SCAN, "new", fileName);

        final File file = new File(new File(Minecraft.getInstance().gameDirectory, Constants.MOD_ID), structureName.toString() + Structures.SCHEMATIC_EXTENSION_NEW);
        Utils.checkDirectory(file.getParentFile());

        try (final OutputStream outputstream = new FileOutputStream(file))
        {
            NbtIo.writeCompressed(CompoundNBT, outputstream);
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
}
