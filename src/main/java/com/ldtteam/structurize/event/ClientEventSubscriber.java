package com.ldtteam.structurize.event;

import com.ldtteam.structures.client.StructureClientHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.BoxRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        final Structure structure = Settings.instance.getActiveStructure();
        final ClientPlayerEntity player = Minecraft.getInstance().player;

        if (structure != null)
        {
            StructureClientHandler.renderStructure(structure, event.getPartialTicks(), Settings.instance.getPosition(), event.getMatrixStack());

            final BlockPos primaryOffset = BlueprintUtils.getPrimaryBlockOffset(structure.getBluePrint());
            final BlockPos offset = primaryOffset.rotate(BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));
            BlockPos pos = Settings.instance.getPosition().subtract(offset);

            BlockPos size = structure.getSize(BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()), Settings.instance.getMirror());

            final BlockPos smallOffset;
            final boolean mirrored = Settings.instance.getMirror() != Mirror.NONE;
            switch (Settings.instance.getRotation())
            {
                case 1:
                    size = new BlockPos(-size.getX(), size.getY(), size.getZ());
                    smallOffset = new BlockPos(-1, 1, 1);
                    if (mirrored)
                    {
                        pos = pos.add(0, 0, -size.getZ() + (2 * offset.getZ()) + 1);
                    }
                    break;
                case 2:
                    if (mirrored)
                    {
                        pos = pos.add(size.getX() - (2 * primaryOffset.getX()) - 1, 0, 0);
                    }
                    size = new BlockPos(-size.getX(), size.getY(), -size.getZ());
                    smallOffset = new BlockPos(-1, 1, -1);
                    break;
                case 3:
                    if (mirrored)
                    {
                        pos = pos.add(0, 0, size.getZ() + (2 * offset.getZ()) - 1);
                    }
                    size = new BlockPos(size.getX(), size.getY(), -size.getZ());
                    smallOffset = new BlockPos(1, 1, -1);
                    break;
                default:
                    if (mirrored)
                    {
                        pos = pos.add(-size.getX() + (2 * primaryOffset.getX()) + 1, 0, 0);
                    }
                    smallOffset = new BlockPos(1, 1, 1);
                    break;
            }

            renderBox(pos, pos.add(size).subtract(smallOffset), player, event);
        }

        if (Settings.instance.getBox() != null)
        {
            renderBox(Settings.instance.getBox().getA(), Settings.instance.getBox().getB(), player, event);
        }
    }

    private static void renderBox(final BlockPos posA, final BlockPos posB, final ClientPlayerEntity player, final RenderWorldLastEvent event)
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

        final ActiveRenderInfo activeRenderInfo = Minecraft.getInstance().getRenderManager().info;
        final Vec3d viewPosition = activeRenderInfo.getProjectedView();
        final MatrixStack matrix = event.getMatrixStack();
        matrix.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);

        matrix.push();
        final Matrix4f matrix4f = matrix.peek().getModel();
        final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        BoxRenderer.drawSelectionBoundingBox(matrix4f, axisalignedbb.grow(0.002D), 1.0F, 1.0F, 1.0F, 1.0F);
        matrix.pop();
    }
}
