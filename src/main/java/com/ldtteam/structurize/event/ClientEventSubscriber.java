package com.ldtteam.structurize.event;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.ldtteam.blockout.hooks.HookManager;
import com.ldtteam.blockout.hooks.HookRegistries;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structures.client.StructureClientHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.ldtteam.structurize.util.RenderUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventSubscriber
{
    public static final RenderTypeBuffers renderBuffers = new RenderTypeBuffers();

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        Settings.instance.startStructurizePass();
        OptifineCompat.getInstance().preBlueprintDraw();

        final MatrixStack matrixStack = event.getMatrixStack();
        final float partialTicks = event.getPartialTicks();

        final IRenderTypeBuffer.Impl renderBuffer = renderBuffers.getBufferSource();
        final Supplier<IVertexBuilder> linesWithCullAndDepth = () -> renderBuffer.getBuffer(RenderType.getLines());
        final Supplier<IVertexBuilder> linesWithoutCullAndDepth = () -> renderBuffer.getBuffer(RenderUtils.LINES_GLINT);

        final PlayerEntity player = Minecraft.getInstance().player;
        final Blueprint blueprint = Settings.instance.getActiveStructure();

        if (blueprint != null)
        {
            Minecraft.getInstance().getProfiler().startSection("struct_render");

            final BlockPos pos = Settings.instance.getPosition();
            final BlockPos posMinusOffset = pos.subtract(blueprint.getPrimaryBlockOffset());

            StructureClientHandler.renderStructure(blueprint, partialTicks, pos, matrixStack);
            renderAnchorPos(pos, matrixStack, linesWithoutCullAndDepth.get());
            RenderUtils.renderWhiteOutlineBox(posMinusOffset,
                posMinusOffset.add(blueprint.getSizeX() - 1, blueprint.getSizeY() - 1, blueprint.getSizeZ() - 1),
                matrixStack,
                linesWithCullAndDepth.get());
            renderBuffer.finish(RenderType.getLines());
            renderBuffer.finish(RenderUtils.LINES_GLINT);

            Minecraft.getInstance().getProfiler().endSection();
        }

        if (Settings.instance.getBox() != null)
        {
            Minecraft.getInstance().getProfiler().startSection("struct_box");

            // Used to render a red box around a scan's Primary offset (primary block)
            Settings.instance.getAnchorPos().ifPresent(pos -> renderAnchorPos(pos, matrixStack, linesWithoutCullAndDepth.get()));
            RenderUtils.renderWhiteOutlineBox(Settings.instance.getBox().getA(),
                Settings.instance.getBox().getB(),
                matrixStack,
                linesWithoutCullAndDepth.get());
            renderBuffer.finish(RenderUtils.LINES_GLINT);

            Minecraft.getInstance().getProfiler().endSection();
        }

        final ItemStack itemStack = player.getHeldItem(Hand.MAIN_HAND);
        if (itemStack.getItem() == ModItems.tagTool && itemStack.getOrCreateTag().contains(ItemTagTool.TAG_ANCHOR_POS))
        {
            final BlockPos tagAnchor = BlockPosUtil.readFromNBT(itemStack.getTag(), ItemTagTool.TAG_ANCHOR_POS);
            final TileEntity te = Minecraft.getInstance().player.world.getTileEntity(tagAnchor);

            renderAnchorPos(tagAnchor, matrixStack, linesWithoutCullAndDepth.get());

            if (te instanceof IBlueprintDataProvider)
            {
                final Map<BlockPos, List<String>> tagPosList = ((IBlueprintDataProvider) te).getWorldTagPosMap();
    
                for (final Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
                {
                    RenderUtils.renderWhiteOutlineBox(entry.getKey(), entry.getKey(), matrixStack, linesWithoutCullAndDepth.get());

                    IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
                    RenderUtils.renderDebugText(entry.getKey(), entry.getValue(), matrixStack, true, 3, buffer);
                    RenderSystem.disableDepthTest();
                    buffer.finish();
                    RenderSystem.enableDepthTest();
                }
    
                renderBuffer.finish(RenderUtils.LINES_GLINT);
            }
        }

        renderBuffer.finish();

        OptifineCompat.getInstance().postBlueprintDraw();
        Settings.instance.endStructurizePass();

        final Vector3d viewPosition = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
        matrixStack.push();
        matrixStack.translate(-viewPosition.getX(), -viewPosition.getY(), -viewPosition.getZ());
        HookRegistries.render(matrixStack, partialTicks);
        matrixStack.pop();
    }

    /**
     * Render a box around the given position in the Red colour.
     *
     * @param anchorPos The anchorPos
     */
    private static void renderAnchorPos(final BlockPos anchorPos, final MatrixStack ms, final IVertexBuilder buffer)
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

        Minecraft.getInstance().getProfiler().startSection("structurize");

        if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % (Constants.TICKS_SECOND * 5) == 0)
        {
            Minecraft.getInstance().getProfiler().startSection("blueprint_manager_tick");
            BlueprintHandler.getInstance().cleanCache();
            Minecraft.getInstance().getProfiler().endSection();
        }
        if (Minecraft.getInstance().world != null)
        {
            Minecraft.getInstance().getProfiler().startSection("hook_manager_tick");
            HookRegistries.tick(Minecraft.getInstance().world.getGameTime());
            Minecraft.getInstance().getProfiler().endSection();
        }

        Minecraft.getInstance().getProfiler().endSection();
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
