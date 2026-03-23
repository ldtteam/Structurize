package com.ldtteam.structurize.client.rendertask.task;

import com.ldtteam.structurize.client.rendercontext.WorldEventRenderContext;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IRenderTask extends IClientTask
{
    /**
     * Indicate the render data it should continue rendering.
     */
    void render(final WorldEventRenderContext context);

    /**
     * Precheck for the render stage, as render(WorldEventRenderContext) is run for all stages
     *
     * @return true if rendering active for this stage
     */
    boolean shouldRenderIn(RenderLevelStageEvent.Stage renderStage);
}
