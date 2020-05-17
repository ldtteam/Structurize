package com.ldtteam.structurize.event;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structures.client.StructureClientHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.items.ItemTagTool;
import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.BoxRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ClientEventSubscriber
{
    public static Map<BlockPos, List<String>> tagPosList = null;
    public static BlockPos                    tagAnchor;

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        Minecraft.getInstance().getProfiler().startSection("struct_render");
        final Blueprint blueprint = Settings.instance.getActiveStructure();

        if (blueprint != null)
        {
            BlockPos offset = new BlockPos(0, 0, 0);
            final BlockPos primaryOffset = BlueprintUtils.getPrimaryBlockOffset(blueprint);

            StructureClientHandler.renderStructure(blueprint,
                event.getPartialTicks(),
                Settings.instance.getPosition().subtract(offset),
                event.getMatrixStack());

            final BlockPos pos = Settings.instance.getPosition().subtract(primaryOffset);
            final BlockPos size = new BlockPos(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());

            Minecraft.getInstance().getProfiler().endStartSection("struct_box");

            // Used to render a red box around a structures Primary offset (primary block)
            renderAnchorPos(primaryOffset.add(pos), event);

            renderBox(pos.subtract(offset), pos.add(size).subtract(new BlockPos(1, 1, 1)).subtract(offset), event);
        }

        if (Settings.instance.getBox() != null)
        {
            // Used to render a red box around a scan's Primary offset (primary block)
            Settings.instance.getAnchorPos().ifPresent(pos -> renderAnchorPos(pos, event));
            Minecraft.getInstance().getProfiler().endStartSection("struct_box");
            renderBox(Settings.instance.getBox().getA(), Settings.instance.getBox().getB(), event);
        }
        Minecraft.getInstance().getProfiler().endSection();

        final PlayerEntity player = Minecraft.getInstance().player;

        if (player.getHeldItem(Hand.MAIN_HAND).getItem() != ModItems.tagTool)
        {
            tagPosList = null;
            tagAnchor = null;
            return;
        }

        if (tagAnchor == null)
        {
            ItemStack tagtool = player.getHeldItem(Hand.MAIN_HAND);

            if (tagtool.getOrCreateTag().contains(ItemTagTool.TAG_ACHNOR_POS))
            {
                tagAnchor = BlockPosUtil.readFromNBT(tagtool.getOrCreateTag(), ItemTagTool.TAG_ACHNOR_POS);
            }
            else
            {
                return;
            }
        }

        if (tagPosList == null)
        {
            TileEntity te = Minecraft.getInstance().player.world.getTileEntity(tagAnchor);

            if (!(te instanceof IBlueprintDataProvider))
            {
                return;
            }

            IBlueprintDataProvider dataProvider = (IBlueprintDataProvider) te;

            tagPosList = dataProvider.getWorldTagPosMap();
        }

        renderAnchorPos(tagAnchor, event);

        for (Map.Entry<BlockPos, List<String>> entry : tagPosList.entrySet())
        {
            renderBox(entry.getKey(), entry.getKey(), event);
            renderDebugText(entry.getKey(), entry.getValue(), event.getMatrixStack());
        }
    }

    /**
     * Render a box around the given position in the Red colour.
     *
     * @param anchorPos The anchorPos
     * @param event The RenderWorldLastEvent event
     */
    private static void renderAnchorPos(final BlockPos anchorPos,  final RenderWorldLastEvent event)
    {
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        renderBox(anchorPos, anchorPos, event, 1, 0, 0);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    /**
     * Render a white box around two positions
     *
     * @param posA The first Position
     * @param posB The second Position
     * @param event The event
     */
    private static void renderBox(final BlockPos posA,
                                  final BlockPos posB,
                                  final RenderWorldLastEvent event)
    {
        renderBox(posA, posB, event, 1, 1, 1);
    }

    /***
     * Render a box around two positions
     *
     * @param posA First position
     * @param posB Second position
     * @param event The Event
     * @param red red colour float 0 - 1
     * @param green green colour float 0 - 1
     * @param blue blue colour float 0 - 1
     */
    private static void renderBox(final BlockPos posA,
                                  final BlockPos posB,
                                  final RenderWorldLastEvent event,
                                  final float red,
                                  final float green,
                                  final float blue)
    {
        int x1 = posA.getX();
        int y1 = posA.getY();
        int z1 = posA.getZ();

        int x2 = posB.getX();
        int y2 = posB.getY();
        int z2 = posB.getZ();

        if (x1 > x2)
        {
            x1++;
        }
        else
        {
            x2++;
        }

        if (y1 > y2)
        {
            y1++;
        }
        else
        {
            y2++;
        }

        if (z1 > z2)
        {
            z1++;
        }
        else
        {
            z2++;
        }

        RenderSystem.enableDepthTest();

        final ActiveRenderInfo activeRenderInfo = Minecraft.getInstance().getRenderManager().info;
        final Vec3d viewPosition = activeRenderInfo.getProjectedView();
        final MatrixStack matrix = event.getMatrixStack();
        matrix.push();
        matrix.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);

        final Matrix4f matrix4f = matrix.getLast().getMatrix();
        final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        BoxRenderer.drawSelectionBoundingBox(matrix4f, axisalignedbb.grow(0.002D), red, green, blue, 1.0F);
        matrix.pop();

        RenderSystem.disableDepthTest();
    }

    private static void renderDebugText(final BlockPos pos, final String text, final MatrixStack matrixStack)
    {
        final FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
        final Vec3d viewPosition = Minecraft.getInstance().getRenderManager().info.getProjectedView();

        matrixStack.push();
        matrixStack.translate((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5);
        matrixStack.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);

        matrixStack.translate(0.0F, 0.75F, 0.0F);
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);

        final EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        matrixStack.rotate(renderManager.getCameraOrientation());
        matrixStack.scale(-0.014F, -0.014F, 0.014F);
        matrixStack.translate(0.0F, 18F, 0.0F);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
          GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
          GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);

        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();

        final IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        fontrenderer.renderString(text, -fontrenderer.getStringWidth(text) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, true, 0, 15728880);

        buffer.finish();
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    /**
     * Renders the given list of strings, 3 elements a row.
     *
     * @param pos         position to render at
     * @param text        text list
     * @param matrixStack stack to use
     */
    private static void renderDebugText(final BlockPos pos, final List<String> text, final MatrixStack matrixStack)
    {
        final FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
        final Vec3d viewPosition = Minecraft.getInstance().getRenderManager().info.getProjectedView();

        matrixStack.push();
        matrixStack.translate((double) pos.getX() + 0.5, (double) pos.getY(), (double) pos.getZ() + 0.5);
        matrixStack.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);

        matrixStack.translate(0.0F, 0.75F, 0.0F);
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);

        final EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        matrixStack.rotate(renderManager.getCameraOrientation());
        matrixStack.scale(-0.014F, -0.014F, 0.014F);
        matrixStack.translate(0.0F, 18F, 0.0F);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
          GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
          GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);

        final Matrix4f matrix4f = matrixStack.getLast().getMatrix();

        final IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

        List<String> toRender = new ArrayList<>();
        for (final String element : text)
        {
            if (toRender.size() >= 3)
            {
                // Render text
                fontrenderer.renderString(toRender.toString(), -fontrenderer.getStringWidth(toRender.toString()) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, true, 0, 15728880);
                matrixStack.translate(0.0F, 10F, 0.0F);
                toRender.clear();
            }
            toRender.add(element);
        }

        if (!toRender.isEmpty())
        {
            fontrenderer.renderString(toRender.toString(), -fontrenderer.getStringWidth(toRender.toString()) / 2.0f, 0, 0xFFFFFFFF, false, matrix4f, buffer, true, 0, 15728880);
        }

        buffer.finish();
        RenderSystem.disableBlend();
        matrixStack.pop();
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
        if (Minecraft.getInstance().world != null && Minecraft.getInstance().world.getGameTime() % (Constants.TICKS_SECOND * 5) == 0)
        {
            BlueprintHandler.getInstance().cleanCache();
        }
    }
}
