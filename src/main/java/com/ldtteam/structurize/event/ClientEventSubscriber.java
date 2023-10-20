package com.ldtteam.structurize.event;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.util.IScrollableItem;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.BlueprintRenderer.TransparencyHack;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.network.messages.ItemMiddleMouseMessage;
import com.ldtteam.structurize.network.messages.ScanToolTeleportMessage;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ClientEventSubscriber
{
    @SubscribeEvent
    public static void renderWorldLastEvent(final RenderGuiOverlayEvent.Pre event)
    {
        if ((event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type() || event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) && Minecraft.getInstance().screen instanceof BOScreen &&
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
        final Stage when = Structurize.getConfig().getClient().rendererTransparency.get() > TransparencyHack.THRESHOLD ?
            Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS :
            Stage.AFTER_TRANSLUCENT_BLOCKS; // otherwise even worse sorting issues arise
        if (event.getStage() != when)
        {
            return;
        }

        final PoseStack matrixStack = event.getPoseStack();
        final MultiBufferSource.BufferSource bufferSource = WorldRenderMacros.getBufferSource();

        final Minecraft mc = Minecraft.getInstance();
        final Vec3 viewPosition = mc.gameRenderer.getMainCamera().getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-viewPosition.x(), -viewPosition.y(), -viewPosition.z());

        for (final BlueprintPreviewData previewData : RenderingCache.getBlueprintsToRender())
        {
            final Blueprint blueprint = previewData.getBlueprint();

            if (blueprint != null)
            {
                mc.getProfiler().push("struct_render");

                final BlockPos pos = previewData.getPos();
                final BlockPos posMinusOffset = pos.subtract(blueprint.getPrimaryBlockOffset());

                BlueprintHandler.getInstance().draw(previewData, pos, event);
                WorldRenderMacros.renderWhiteLineBox(bufferSource,
                  matrixStack,
                  posMinusOffset,
                  posMinusOffset.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1),
                  0.02f);
                WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, pos, pos, 0.02f);

                mc.getProfiler().pop();
            }
        }

        for (final BoxPreviewData previewData : RenderingCache.getBoxesToRender())
        {
            mc.getProfiler().push("struct_box");

            // Used to render a red box around a scan's Primary offset (primary block)
            WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, previewData.getPos1(), previewData.getPos2(), 0.02f);
            previewData.getAnchor().ifPresent(pos -> WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, pos, pos, 0.02f));

            mc.getProfiler().pop();
        }


        final Player player = mc.player;
        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() == ModItems.tagTool.get() && itemStack.getOrCreateTag().contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            mc.getProfiler().push("struct_tags");

            final BlockPos tagAnchor = BlockPosUtil.readFromNBT(itemStack.getTag(), ItemTagTool.TAG_ANCHOR_POS);
            final BlockEntity te = player.level().getBlockEntity(tagAnchor);

            if (te instanceof IBlueprintDataProviderBE)
            {
                final Map<BlockPos, List<String>> tagPosList = ((IBlueprintDataProviderBE) te).getWorldTagPosMap();

                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, entry.getKey(), entry.getKey(), 0.02f);
                    WorldRenderMacros.renderDebugText(entry.getKey(), entry.getValue(), matrixStack, true, 3, bufferSource);
                }
            }
            WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, tagAnchor, tagAnchor, 0.02f);

            mc.getProfiler().pop();
        }

        bufferSource.endBatch();
        matrixStack.popPose();
    }

    /**
     * Used to catch the clientTickEvent.
     * Call renderer cache cleaning every 5 secs (100 ticks).
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void onClientTickEvent(final ClientTickEvent event)
    {
        if (event.phase != Phase.END)
        {
            return;
        }

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
                Network.getNetwork().sendToServer(new ScanToolTeleportMessage());
            }
        }

        mc.getProfiler().pop();
    }

    @SubscribeEvent
    public static void onPreClientTickEvent(@NotNull final ClientTickEvent event)
    {
        if (event.phase != Phase.START) return;

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
                        Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(pos, ctrlKey));
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
            switch (scrollableItem.onMouseScroll(mc.player, current, event.getScrollDelta(), ctrlKey))
            {
                case PASS:
                    break;
                case FAIL:
                    event.setCanceled(true);
                    break;
                default:
                    event.setCanceled(true);
                    Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(event.getScrollDelta(), ctrlKey));
                    break;
            }
        }
    }
}
