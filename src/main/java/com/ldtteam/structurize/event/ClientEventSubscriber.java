package com.ldtteam.structurize.event;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.IScrollableItem;
import com.ldtteam.structurize.api.util.ISpecialBlockPickItem;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.BlueprintRenderer;
import com.ldtteam.structurize.client.ModKeyMappings;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.client.rendercontext.WorldEventRenderContext;
import com.ldtteam.structurize.client.rendertask.RenderTaskManager;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.network.messages.ItemMiddleMouseMessage;
import com.ldtteam.structurize.network.messages.ScanToolTeleportMessage;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.ldtteam.structurize.util.ItemStackNbtHelper;
import com.ldtteam.structurize.client.rendertask.util.BufferSourceCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.resources.Identifier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ClientEventSubscriber
{
    @SubscribeEvent
    public static void hideVanillaStatusOverlays(final RenderGuiLayerEvent.Pre event)
    {
        final Identifier layer = event.getName();
        if ((layer.equals(VanillaGuiLayers.PLAYER_HEALTH) || layer.equals(VanillaGuiLayers.FOOD_LEVEL)) && Minecraft.getInstance().gui.screen() instanceof BOScreen &&
              ((BOScreen) Minecraft.getInstance().gui.screen()).getWindow() instanceof WindowExtendedBuildTool)
        {
             event.setCanceled(true);
        }
    }


    /**
     * Submit build-tool previews while the level renderer is collecting the
     * current frame's feature nodes. RenderLevelStageEvent is fired after the
     * feature frame has already been prepared, so submissions made there are
     * discarded by Minecraft 26.2.
     */
    @SubscribeEvent
    public static void submitBlueprints(final SubmitCustomGeometryEvent event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
        {
            return;
        }

        try (final Gizmos.TemporaryCollection ignored = mc.levelRenderer.collectPerFrameRenderThreadGizmos())
        {
            for (final BlueprintPreviewData previewData : RenderingCache.getBlueprintsToRender())
            {
                final Blueprint blueprint = previewData.getBlueprint();
                final BlockPos pos = previewData.getPos();
                if (blueprint == null || pos == null)
                {
                    continue;
                }

                BlueprintHandler.getInstance().internalBackportDraw(previewData, pos, event);

                final BlockPos anchor = pos.subtract(blueprint.getPrimaryBlockOffset());
                final BlockPos max = anchor.offset(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());
                Gizmos.cuboid(new AABB(Vec3.atLowerCornerOf(anchor), Vec3.atLowerCornerOf(max)), GizmoStyle.stroke(0xffffffff, 2.5f));
                Gizmos.cuboid(new AABB(pos), GizmoStyle.stroke(0xffff0000, 2.5f)).setAlwaysOnTop();
            }
        }
    }

    @SubscribeEvent
    public static void renderAfterBlockFeatures(final RenderLevelStageEvent.AfterOpaqueFeatures event)
    {
        WorldEventRenderContext.INSTANCE.renderWorldLastEvent(event);
        renderTagTool(Minecraft.getInstance(), event.getPoseStack(), WorldRenderMacros.getBufferSource(),
            Minecraft.getInstance().gameRenderer.mainCamera().position());
        WorldRenderMacros.getBufferSource().endBatch();
    }

    private static void renderTagTool(final Minecraft mc,
        final PoseStack matrixStack,
        final BufferSourceCompat bufferSource,
        final Vec3 viewPosition)
    {
        final Player player = mc.player;
        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() == ModItems.tagTool.get() && ItemStackNbtHelper.hasCustomTag(itemStack)
            && ItemStackNbtHelper.getCustomTag(itemStack).contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            Profiler.get().push("struct_tags");

            final BlockPos tagAnchor = BlockPosUtil.readFromNBT(ItemStackNbtHelper.getCustomTag(itemStack), ItemTagTool.TAG_ANCHOR_POS);
            final Vec3 realRenderRootVecd = Vec3.atLowerCornerOf(tagAnchor).subtract(viewPosition);
            final BlockEntity te = player.level().getBlockEntity(tagAnchor);

            matrixStack.pushPose();
            matrixStack.translate(realRenderRootVecd.x(), realRenderRootVecd.y(), realRenderRootVecd.z());

            if (te instanceof final IBlueprintDataProviderBE blueprintProvider)
            {
                final Map<BlockPos, List<String>> tagPosList = blueprintProvider.getWorldTagPosMap();

                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    final BlockPos pos = entry.getKey().subtract(tagAnchor);
                    WorldRenderMacros.renderWhiteLineBox(bufferSource, matrixStack, pos, pos, 0.025f);
                    WorldRenderMacros.renderDebugText(pos, entry.getKey(), entry.getValue(), matrixStack, true, 3, bufferSource);
                }
            }
            WorldRenderMacros.renderRedGlintLineBox(bufferSource, matrixStack, BlockPos.ZERO, BlockPos.ZERO, 0.025f);

            matrixStack.popPose();

            Profiler.get().pop();
        }
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
        Profiler.get().push("structurize");

        if (mc.level != null && mc.level.getGameTime() % (Constants.TICKS_SECOND * BlueprintHandler.CACHE_EXPIRE_CHECK_SECONDS) == 0)
        {
            Profiler.get().push("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            Profiler.get().pop();
        }

        if (ModKeyMappings.TELEPORT.get().consumeClick() && mc.level != null && mc.player != null &&
            mc.player.getMainHandItem().getItem() instanceof ItemScanTool tool)
        {
            if (tool.onTeleport(mc.player, mc.player.getMainHandItem()))
            {
                Network.getNetwork().sendToServer(new ScanToolTeleportMessage());
            }
        }

        Profiler.get().pop();
    }

    @SubscribeEvent
    public static void onPreClientTickEvent(@NotNull final ClientTickEvent.Pre event)
    {
        RenderTaskManager.onClientTick();

        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null || mc.level == null) return;

        if (mc.options.keyPickItem.consumeClick())
        {
            BlockPos pos = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult)mc.hitResult).getBlockPos() : null;
            if (pos != null && mc.level.getBlockState(pos).isAir())
            {
                pos = null;
            }

            final ItemStack current = mc.player.getInventory().getSelectedItem();
            if (current.getItem() instanceof ISpecialBlockPickItem clickableItem)
            {
                final boolean ctrlKey = mc.hasControlDown();
                    final InteractionResult pickResult = clickableItem.onBlockPick(mc.player, current, pos, ctrlKey);
                    if (pickResult == InteractionResult.PASS)
                    {
                        KeyMapping.click(mc.options.keyPickItem.getKey());
                    }
                    else if (pickResult != InteractionResult.FAIL)
                    {
                        KeyMapping.click(mc.options.keyPickItem.getKey());
                        Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(pos, ctrlKey));
                    }
            }
            else
            {
                KeyMapping.click(mc.options.keyPickItem.getKey());
            }
        }
    }

    @SubscribeEvent
    public static void onMouseWheel(final InputEvent.MouseScrollingEvent event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (event.isCanceled() || mc.player == null || mc.gui.screen() != null || mc.level == null) return;
        if (!mc.player.isShiftKeyDown()) return;

        final ItemStack current = mc.player.getInventory().getSelectedItem();
        if (current.getItem() instanceof IScrollableItem scrollableItem)
        {
            final boolean ctrlKey = mc.hasControlDown();
            final InteractionResult scrollResult =
                scrollableItem.onMouseScroll(mc.player, current, event.getScrollDeltaY(), ctrlKey);
            if (scrollResult == InteractionResult.FAIL)
            {
                event.setCanceled(true);
            }
            else if (scrollResult != InteractionResult.PASS)
            {
                event.setCanceled(true);
                Network.getNetwork().sendToServer(new ItemMiddleMouseMessage(event.getScrollDeltaY(), ctrlKey));
            }
        }
    }
}
