package com.ldtteam.structurize.event;

import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structures.client.StructureClientHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.ldtteam.structurize.util.BoxRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class ClientEventSubscriber
{
    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        Minecraft.getInstance().getProfiler().startSection("struct_render");
        final Structure structure = Settings.instance.getActiveStructure();
        final ClientPlayerEntity player = Minecraft.getInstance().player;

        if (structure != null)
        {
            BlockPos offset = new BlockPos(0, 0, 0);
            final Tuple<BlockPos, Boolean> primaryOffset = BlueprintUtils.getPrimaryBlockOffset(structure.getBluePrint());

            if (!primaryOffset.getB())
            {
                switch (Settings.instance.getRotation())
                {
                    case 1:
                        if (Settings.instance.getMirror() == Mirror.FRONT_BACK && structure.getBluePrint().getSizeZ() % 2 == 0)
                        {
                            offset = offset.north();
                        }
                        if (structure.getBluePrint().getSizeX() % 2 == 0)
                        {
                            offset = offset.west();
                        }
                        break;

                    case 2:
                        if (Settings.instance.getMirror() != Mirror.FRONT_BACK && structure.getBluePrint().getSizeX() % 2 == 0)
                        {
                            offset = offset.west();
                        }
                        if (structure.getBluePrint().getSizeZ() % 2 == 0)
                        {
                            offset = offset.north();
                        }
                        break;

                    case 3:
                        if (structure.getBluePrint().getSizeZ() % 2 == 0)
                        {
                            if (Settings.instance.getMirror() == Mirror.FRONT_BACK)
                            {
                                offset = offset.south();
                            }
                            offset = offset.north();
                        }
                        break;

                    default:
                        if (structure.getBluePrint().getSizeX() % 2 == 0)
                        {
                            if (Settings.instance.getMirror() == Mirror.FRONT_BACK)
                            {
                                offset = offset.west();
                            }
                        }
                        break;
                }
            }

            StructureClientHandler.renderStructure(structure,
                event.getPartialTicks(),
                Settings.instance.getPosition().subtract(offset),
                event.getMatrixStack());

            final BlockPos pos = Settings.instance.getPosition().subtract(primaryOffset.getA());
            final BlockPos size = new BlockPos(structure.getBluePrint().getSizeX(),
                structure.getBluePrint().getSizeY(),
                structure.getBluePrint().getSizeZ());

            Minecraft.getInstance().getProfiler().endStartSection("struct_box");

            // Used to render a red box around a structures Primary offset (primary block)
            renderAnchorPos(primaryOffset.getA().add(pos), event);

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
