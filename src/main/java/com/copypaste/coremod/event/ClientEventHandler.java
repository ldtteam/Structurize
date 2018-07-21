package com.copypaste.coremod.event;

import com.copypaste.structures.helpers.Settings;
import com.copypaste.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
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
        final WorldClient world = Minecraft.getMinecraft().world;
        final EntityPlayer player = Minecraft.getMinecraft().player;

        if (Settings.instance.getBox() != null)
        {
            final BlockPos posA = Settings.instance.getBox().getFirst();
            final BlockPos posB = Settings.instance.getBox().getSecond();

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

            final double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
            final double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
            final double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();

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
}
