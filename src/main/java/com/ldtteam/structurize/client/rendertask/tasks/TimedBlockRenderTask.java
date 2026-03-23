package com.ldtteam.structurize.client.rendertask.tasks;

import com.ldtteam.structurize.client.rendercontext.WorldEventRenderContext;
import com.ldtteam.structurize.client.rendertask.task.IRenderTask;
import com.ldtteam.structurize.client.rendertask.task.TimedTask;
import com.ldtteam.structurize.client.rendertask.util.WorldRenderMacros;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.client.rendertask.util.RenderTypes.LINES_WITH_WIDTH;

/**
 * Highlight render data for marking blocks in the world with potential warnings on them.
 */
public class TimedBlockRenderTask extends TimedTask implements IRenderTask
{
    /**
     * List of texts to display.
     */
    private final List<String> text = new ArrayList<>();

    /**
     * Position where to render the box.
     */
    private final BlockPos pos;

    /**
     * The colour at which the box should render.
     */
    private int argbColor = 0xffffffff;

    /**
     * Default constructor.
     */
    public TimedBlockRenderTask(final String id, final BlockPos pos, final int seconds)
    {
        super(id, seconds);
        this.pos = pos;
    }

    @Override
    public void render(final WorldEventRenderContext context)
    {
        if (context.clientPlayer.blockPosition().distSqr(pos) > 50 * 50)
        {
            return;
        }

        WorldRenderMacros.renderLineBox(context.bufferSource.getBuffer(LINES_WITH_WIDTH),
            context.poseStack,
            pos,
            pos,
            (argbColor >> 16) & 0xff,
            (argbColor >> 8) & 0xff,
            argbColor & 0xff,
            (argbColor >> 24) & 0xff,
            0.025f);

        if (!text.isEmpty())
        {
            WorldRenderMacros.renderDebugText(pos, text, context.poseStack, true, 3, context.bufferSource);
        }
    }

    @Override
    public boolean shouldRenderIn(final RenderLevelStageEvent.Stage renderStage)
    {
        return renderStage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES;
    }

    /**
     * List of strings to display
     */
    public void addText(final String text)
    {
        this.text.add(text);
    }

    /**
     * Color code for the box, argb format
     */
    public void setColor(final int argbColor)
    {
        this.argbColor = argbColor;
    }
}
