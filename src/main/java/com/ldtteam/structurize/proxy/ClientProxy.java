package com.ldtteam.structurize.proxy;

import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Client side proxy.
 */
public class ClientProxy implements IProxy
{
    @Override
    @SuppressWarnings("resource")
    public void openBuildToolWindow(@Nullable final BlockPos pos, final int groundstyle)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        if (Minecraft.getInstance().screen != null)
        {
            return;
        }

        @Nullable
        final WindowBuildTool window = new WindowBuildTool(pos, groundstyle);
        window.open();
    }

    @Override
    @SuppressWarnings("resource")
    public void openExtendedBuildToolWindow(@Nullable final BlockPos pos, final int groundstyle)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        if (Minecraft.getInstance().screen != null)
        {
            return;
        }

        @Nullable
        final WindowExtendedBuildTool window = new WindowExtendedBuildTool(pos, groundstyle);
        window.open();
    }

    @Override
    public void openShapeToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable
        final WindowShapeTool window = new WindowShapeTool(pos);
        window.open();
    }

    @Override
    @SuppressWarnings("resource")
    public File getSchematicsFolder()
    {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            if (Manager.getServerUUID() != null)
            {
                return new File(Minecraft.getInstance().gameDirectory, Constants.MOD_ID + "/" + Manager.getServerUUID());
            }
            else
            {
                Log.getLogger().error("Manager.getServerUUID() => null this should not happen");
                return null;
            }
        }

        // if the world schematics folder exists we use it
        // otherwise we use the minecraft folder /structurize/schematics if on the physical client on the logical server
        final File worldSchematicFolder = new File(
            ServerLifecycleHooks.getCurrentServer().getServerDirectory() + "/" + Constants.MOD_ID + '/' + Structures.SCHEMATICS_PREFIX);

        if (!worldSchematicFolder.exists())
        {
            return new File(Minecraft.getInstance().gameDirectory, Constants.MOD_ID);
        }

        return worldSchematicFolder.getParentFile();
    }
}
