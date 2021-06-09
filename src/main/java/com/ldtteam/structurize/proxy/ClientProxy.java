package com.ldtteam.structurize.proxy;

import java.io.File;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import org.jetbrains.annotations.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientProxy implements IProxy
{
    @Override
    public void openBuildToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        if (Minecraft.getInstance().screen instanceof Screen)
        {
            return;
        }

        @Nullable
        final WindowBuildTool window = new WindowBuildTool(pos);
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

    @Override
    public BlockState getBlockStateFromWorld(final BlockPos pos)
    {
        return Minecraft.getInstance().level.getBlockState(pos);
    }
}
