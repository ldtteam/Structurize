package com.ldtteam.structurize.client.rendercontext;

import com.ldtteam.structurize.client.rendertask.RenderTaskManager;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/**
 * Main class for handling world rendering.
 * Also holds all possible values which may be needed during rendering.
 */
public class WorldEventRenderContext
{
    public static final WorldEventRenderContext INSTANCE = new WorldEventRenderContext();

    private WorldEventRenderContext()
    {
        // singleton
    }

    public RenderLevelStageEvent stageEvent;
    public BufferSource          bufferSource;
    public PoseStack             poseStack;
    public float                 partialTicks;
    public ClientLevel           clientLevel;
    public LocalPlayer           clientPlayer;
    public ItemStack             mainHandItem;

    /**
     * In chunks
     */
    int clientRenderDist;

    public void renderWorldLastEvent(final RenderLevelStageEvent event)
    {
        stageEvent = event;
        bufferSource = WorldRenderMacros.getBufferSource();
        poseStack = event.getPoseStack();
        partialTicks = event.getPartialTick();
        clientLevel = Minecraft.getInstance().level;
        clientPlayer = Minecraft.getInstance().player;
        mainHandItem = clientPlayer.getMainHandItem();
        clientRenderDist = Minecraft.getInstance().options.renderDistance().get();

        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        runRenderTasks(event);

        bufferSource.endBatch();

        poseStack.popPose();
    }

    private void runRenderTasks(final RenderLevelStageEvent event)
    {
        RenderTaskManager.render(this);
    }
}
