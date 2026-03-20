package com.ldtteam.structurize.client.rendertask.tasks;

import com.ldtteam.structurize.client.rendercontext.WorldEventRenderContext;
import com.ldtteam.structurize.client.rendertask.task.IRenderTask;
import com.ldtteam.structurize.client.rendertask.task.TimedTask;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/**
 * Preview data for box contexts.
 */
public class BoxPreviewRenderTask extends TimedTask implements IRenderTask
{
    private final BoxPreviewData data;

    /**
     * Create a new box preview task
     */
    public BoxPreviewRenderTask(final String id, final BoxPreviewData data, final int seconds)
    {
        super(id, seconds);
        this.data = data;
    }

    @Override
    public void render(final WorldEventRenderContext context)
    {
        // Used to render a red box around a scan's Primary offset (primary block)
        WorldRenderMacros.renderWhiteLineBox(context.bufferSource, context.poseStack, data.getPos1(), data.getPos2(), 0.025f);
        data.getAnchor().map(pos -> pos).ifPresent(pos -> WorldRenderMacros.renderRedGlintLineBox(context.bufferSource, context.poseStack, pos, pos, 0.025f));
    }

    @Override
    public boolean shouldRenderIn(final RenderLevelStageEvent.Stage renderStage)
    {
        return renderStage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES;
    }
}
