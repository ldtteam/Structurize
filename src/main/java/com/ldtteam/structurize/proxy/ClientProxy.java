package com.ldtteam.structurize.proxy;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.client.gui.WindowMultiBlock;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.client.gui.WindowScan;
import com.ldtteam.structurize.client.gui.WindowShapeTool;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.optifine.OptifineCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public void openBuildToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        if (Minecraft.getInstance().currentScreen instanceof Screen)
        {
            return;
        }

        @Nullable final WindowBuildTool window = new WindowBuildTool(pos);
        window.open();
    }

    @Override
    public void openShapeToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowShapeTool window = new WindowShapeTool(pos);
        window.open();
    }

    @Override
    public void openScanToolWindow(@Nullable final BlockPos pos1, @Nullable final BlockPos pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return;
        }

        @Nullable final WindowScan window = new WindowScan(pos1, pos2);
        window.open();
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        if (Minecraft.getInstance().currentScreen instanceof Screen)
        {
            return;
        }

        @Nullable final WindowBuildTool window = new WindowBuildTool(pos, structureName, rotation, null);
        window.open();
    }

    @Override
    public File getSchematicsFolder()
    {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            if (Manager.getServerUUID() != null)
            {
                return new File(Minecraft.getInstance().gameDir, Constants.MOD_ID + "/" + Manager.getServerUUID());
            }
            else
            {
                Log.getLogger().error("Manager.getServerUUID() => null this should not happen");
                return null;
            }
        }

        // if the world schematics folder exists we use it
        // otherwise we use the minecraft folder /structurize/schematics if on the physical client on the logical server
        final File worldSchematicFolder =
            new File(ServerLifecycleHooks.getCurrentServer().getDataDirectory() + "/" + Constants.MOD_ID + '/' + Structures.SCHEMATICS_PREFIX);

        if (!worldSchematicFolder.exists())
        {
            return new File(Minecraft.getInstance().gameDir, Constants.MOD_ID);
        }

        return worldSchematicFolder.getParentFile();
    }

    @Nullable
    @Override
    public World getWorld(final int dimension)
    {
        return Minecraft.getInstance().world;
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final PlayerEntity player)
    {
        if (player instanceof ClientPlayerEntity)
        {
            return ((ClientPlayerEntity) player).getRecipeBook();
        }

        return super.getRecipeBookFromPlayer(player);
    }

    @Override
    public void openMultiBlockWindow(@Nullable final BlockPos pos)
    {
        @Nullable final WindowMultiBlock window = new WindowMultiBlock(pos);
        window.open();
    }
}
