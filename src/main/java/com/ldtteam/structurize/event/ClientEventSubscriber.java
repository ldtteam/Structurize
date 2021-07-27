package com.ldtteam.structurize.event;

import com.ldtteam.blockui.hooks.HookManager;
import com.ldtteam.blockui.hooks.HookRegistries;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.BlueprintHandler;
import com.ldtteam.structurize.client.StructureClientHandler;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderBuffers;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ClientEventSubscriber
{
    public static final RenderBuffers renderBuffers = new RenderBuffers();

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(final RenderWorldLastEvent event)
    {
        Settings.instance.startStructurizePass();
        OptifineCompat.getInstance().preBlueprintDraw();

        final PoseStack matrixStack = event.getMatrixStack();
        final float partialTicks = event.getPartialTicks();

        final MultiBufferSource.BufferSource renderBuffer = renderBuffers.bufferSource();
        final Supplier<VertexConsumer> linesWithCullAndDepth = () -> renderBuffer.getBuffer(RenderType.lines());
        final Supplier<VertexConsumer> linesWithoutCullAndDepth = () -> renderBuffer.getBuffer(RenderUtils.LINES_GLINT);

        final Player player = Minecraft.getInstance().player;
        final Blueprint blueprint = Settings.instance.getActiveStructure();

        if (blueprint != null)
        {
            Minecraft.getInstance().getProfiler().push("struct_render");

            final BlockPos pos = Settings.instance.getPosition();
            final BlockPos posMinusOffset = pos.subtract(blueprint.getPrimaryBlockOffset());

            RenderSystem.applyModelViewMatrix();
            StructureClientHandler.renderStructure(blueprint, partialTicks, pos, matrixStack);
            renderAnchorPos(pos, matrixStack, linesWithoutCullAndDepth.get());
            RenderUtils.renderWhiteOutlineBox(posMinusOffset,
                posMinusOffset.offset(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1),
                matrixStack,
                linesWithCullAndDepth.get());

            renderBuffer.endBatch(RenderType.lines());
            renderBuffer.endBatch(RenderUtils.LINES_GLINT);

            Minecraft.getInstance().getProfiler().pop();
        }

        if (Settings.instance.getBox() != null)
        {
            Minecraft.getInstance().getProfiler().push("struct_box");

            // Used to render a red box around a scan's Primary offset (primary block)
            Settings.instance.getAnchorPos().ifPresent(pos -> renderAnchorPos(pos, matrixStack, linesWithoutCullAndDepth.get()));
            RenderUtils.renderWhiteOutlineBox(Settings.instance.getBox().getA(),
                Settings.instance.getBox().getB(),
                matrixStack,
                linesWithoutCullAndDepth.get());

            renderBuffer.endBatch(RenderUtils.LINES_GLINT);

            Minecraft.getInstance().getProfiler().pop();
        }

        final ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.getItem() == ModItems.tagTool.get() && itemStack.getOrCreateTag().contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            final BlockPos tagAnchor = BlockPosUtil.readFromNBT(itemStack.getTag(), ItemTagTool.TAG_ANCHOR_POS);
            final BlockEntity te = Minecraft.getInstance().player.level.getBlockEntity(tagAnchor);

            renderAnchorPos(tagAnchor, matrixStack, linesWithoutCullAndDepth.get());

            if (te instanceof IBlueprintDataProvider)
            {
                final Map<BlockPos, List<String>> tagPosList = ((IBlueprintDataProvider) te).getWorldTagPosMap();
    
                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    RenderUtils.renderWhiteOutlineBox(entry.getKey(), entry.getKey(), matrixStack, linesWithoutCullAndDepth.get());

                    MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                    RenderUtils.renderDebugText(entry.getKey(), entry.getValue(), matrixStack, true, 3, buffer);
                    RenderSystem.disableDepthTest();
                    buffer.endBatch();
                    RenderSystem.enableDepthTest();
                }
    
                renderBuffer.endBatch(RenderUtils.LINES_GLINT);
            }
        }

        renderBuffer.endBatch();

        OptifineCompat.getInstance().postBlueprintDraw();
        Settings.instance.endStructurizePass();

        final Vec3 viewPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrixStack.pushPose();
        matrixStack.translate(-viewPosition.x(), -viewPosition.y(), -viewPosition.z());
        HookRegistries.render(matrixStack, partialTicks);
        matrixStack.popPose();
    }

    /**
     * Render a box around the given position in the Red colour.
     *
     * @param anchorPos The anchorPos
     */
    private static void renderAnchorPos(final BlockPos anchorPos, final PoseStack ms, final VertexConsumer buffer)
    {
        RenderUtils.renderBox(anchorPos, anchorPos, 1, 0, 0, 1, 0, ms, buffer);
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

        Minecraft.getInstance().getProfiler().push("structurize");

        if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getGameTime() % (Constants.TICKS_SECOND * 5) == 0)
        {
            Minecraft.getInstance().getProfiler().push("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            Minecraft.getInstance().getProfiler().pop();
        }
        if (Minecraft.getInstance().level != null)
        {
            Minecraft.getInstance().getProfiler().push("hook_manager_tick");
            HookRegistries.tick(Minecraft.getInstance().level.getGameTime());
            Minecraft.getInstance().getProfiler().pop();
        }

        Minecraft.getInstance().getProfiler().pop();
    }

    /**
     * Used to catch the scroll when no gui is open.
     *
     * @param event the catched event.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrollEvent(final MouseScrollEvent event)
    {
        // cancel in-game scrolling when raytraced gui has scrolling list
        event.setCanceled(HookManager.onScroll(event.getScrollDelta()));
    }
}
