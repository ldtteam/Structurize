package com.ldtteam.structurize.event;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.api.IScrollableItem;
import com.ldtteam.structurize.api.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.constants.Constants;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.network.messages.ItemMiddleMouseMessage;
import com.ldtteam.structurize.network.messages.ScanToolTeleportMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;

public class ClientEventSubscriber
{
    @SubscribeEvent
    public static void renderWorldLastEvent(final RenderGuiLayerEvent.Pre event)
    {
        if ((event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH) || event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) && Minecraft.getInstance().screen instanceof BOScreen &&
              ((BOScreen) Minecraft.getInstance().screen).getWindow() instanceof WindowExtendedBuildTool)
        {
             event.setCanceled(true);
        }
    }


    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(final RenderLevelStageEvent event)
    {
        WorldRenderContext.INSTANCE.renderWorldLastEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void finishBuffers(final RenderLevelStageEvent event)
    {
        WorldRenderContext.RenderTypes.finishBuffer(event);
    }

    /**
     * Used to catch the clientTickEvent.
     * Call renderer cache cleaning every 5 secs (100 ticks).
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void onClientTickEvent(final ClientTickEvent.Post event)
    {
        final Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("structurize");

        if (mc.level != null && mc.level.getGameTime() % (Constants.TICKS_SECOND * BlueprintHandler.CACHE_EXPIRE_CHECK_SECONDS) == 0)
        {
            mc.getProfiler().push("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            mc.getProfiler().pop();
        }

        if (ModKeyMappings.TELEPORT.get().consumeClick() && mc.level != null && mc.player != null &&
            mc.player.getMainHandItem().getItem() instanceof ItemScanTool tool)
        {
            if (tool.onTeleport(mc.player, mc.player.getMainHandItem()))
            {
                new ScanToolTeleportMessage().sendToServer();
            }
        }

        mc.getProfiler().pop();
    }

    @SubscribeEvent
    public static void onPreClientTickEvent(@NotNull final ClientTickEvent.Pre event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.level == null) return;

        if (mc.options.keyPickItem.consumeClick())
        {
            BlockPos pos = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult)mc.hitResult).getBlockPos() : null;
            if (pos != null && mc.level.getBlockState(pos).isAir())
            {
                pos = null;
            }

            final ItemStack current = mc.player.getInventory().getSelected();
            if (current.getItem() instanceof ISpecialBlockPickItem clickableItem)
            {
                final boolean ctrlKey = Screen.hasControlDown();
                switch (clickableItem.onBlockPick(mc.player, current, pos, ctrlKey))
                {
                    case PASS:
                        ++mc.options.keyPickItem.clickCount;
                        break;
                    case FAIL:
                        break;
                    default:
                        new ItemMiddleMouseMessage(pos, ctrlKey).sendToServer();
                        break;
                }
            }
            else
            {
                ++mc.options.keyPickItem.clickCount;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseWheel(final InputEvent.MouseScrollingEvent event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (event.isCanceled() || mc.player == null || mc.screen != null || mc.level == null) return;
        if (!mc.player.isShiftKeyDown()) return;

        final ItemStack current = mc.player.getInventory().getSelected();
        if (current.getItem() instanceof IScrollableItem scrollableItem)
        {
            final boolean ctrlKey = Screen.hasControlDown();
            switch (scrollableItem.onMouseScroll(mc.player, current, event.getScrollDeltaX(), event.getScrollDeltaY(), ctrlKey))
            {
                case PASS:
                    break;
                case FAIL:
                    event.setCanceled(true);
                    break;
                default:
                    event.setCanceled(true);
                    new ItemMiddleMouseMessage(event.getScrollDeltaX(), event.getScrollDeltaY(), ctrlKey).sendToServer();
                    break;
            }
        }
    }

    @SubscribeEvent
    public static void onDisconnect(final LoggingOut event)
    {
        // clear local caches
        WindowExtendedBuildTool.clearStaticData();
    }
}
