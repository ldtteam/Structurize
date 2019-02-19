package com.ldtteam.structurize.event;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structures.lib.BlueprintUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;


/**
 * Used to handle client events.
 */
public class ClientEventHandler
{
    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        final Structure structure = Settings.instance.getActiveStructure();
        final EntityPlayer player = Minecraft.getMinecraft().player;

        if (structure != null)
        {
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
            renderBox(Settings.instance.getBox().getFirst(), Settings.instance.getBox().getSecond(), player, event);
        }
    }

    private static void renderBox(final BlockPos posA, final BlockPos posB, final EntityPlayer player, final RenderWorldLastEvent event)
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

        final double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
        final double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
        final double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
          GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
          GlStateManager.SourceFactor.ONE,
          GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        RenderGlobal.drawSelectionBoundingBox(axisalignedbb.grow(0.002D).offset(-renderPosX, -renderPosY, -renderPosZ), 1.0F, 1.0F, 1.0F, 1.0F);


        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}
