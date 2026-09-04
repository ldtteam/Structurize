package com.ldtteam.structurize.client.rendercontext;

import com.ldtteam.structurize.client.rendertask.RenderTaskManager;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import com.ldtteam.structurize.client.rendertask.util.BufferSourceCompat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

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
    public BufferSourceCompat     bufferSource;
    public PoseStack             poseStack;
    public float                 partialTicks;
    public ClientLevel           clientLevel;
    public LocalPlayer           clientPlayer;
    public ItemStack             mainHandItem;

    /**
     * In chunks
     */
    int clientRenderDist;

    public boolean isStage(final Class<? extends RenderLevelStageEvent> stageType)
    {
        return stageType.isInstance(stageEvent);
    }

    public void renderWorldLastEvent(final RenderLevelStageEvent event)
    {
        stageEvent = event;
        bufferSource = WorldRenderMacros.getBufferSource();
        poseStack = event.getPoseStack();
        partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        clientLevel = Minecraft.getInstance().level;
        clientPlayer = Minecraft.getInstance().player;
        mainHandItem = clientPlayer.getMainHandItem();
        clientRenderDist = Minecraft.getInstance().options.renderDistance().get();

        final Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
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
