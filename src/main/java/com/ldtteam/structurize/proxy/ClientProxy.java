package com.ldtteam.structurize.proxy;

import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Client side proxy.
 */
public class ClientProxy implements IProxy
{
    @Override
    @SuppressWarnings("resource")
    public void openBuildToolWindow(@Nullable final BlockPos pos, final int groundstyle)
    {
        if (Minecraft.getInstance().screen != null)
        {
            return;
        }

        @Nullable
        final WindowExtendedBuildTool window = new WindowExtendedBuildTool(pos, groundstyle, null, WindowExtendedBuildTool.BLOCK_BLUEPRINT_REQUIREMENT);
        window.open();
    }

    @Override
    public void openShapeToolWindow(@Nullable final BlockPos pos)
    {
        /*if (pos == null && OldSettings.instance.getActiveStructure() == null)
        {
            todo shapetool
            return;
        }*/

        @Nullable
        final WindowShapeTool window = new WindowShapeTool(pos);
        window.open();
    }
}
