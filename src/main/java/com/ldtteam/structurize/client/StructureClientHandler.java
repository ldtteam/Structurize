package com.ldtteam.structurize.client;

import java.util.List;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;

public final class StructureClientHandler
{

    private StructureClientHandler()
    {
        throw new IllegalArgumentException("Utility class");
    }

    /**
     * Renders blueprint at single position.
     *
     * @param previewData what to render
     * @param pos       where to render
     */
    public static void renderStructureAtPos(final BlueprintPreviewData previewData, final float partialTicks, final BlockPos pos, final PoseStack stack)
    {
        BlueprintHandler.getInstance().draw(previewData, pos, stack, partialTicks);
    }

    /**
     * Renders blueprint at list of positions.
     * @param previewData the blueprint with context to render.
     * @param partialTicks the partial ticks.
     * @param points the list of points.
     * @param stack the matrix stack to render it in.
     */
    public static void renderStructureAtPosList(final BlueprintPreviewData previewData, final float partialTicks, final List<BlockPos> points, final PoseStack stack)
    {
        BlueprintHandler.getInstance().drawAtListOfPositions(previewData, points, stack, partialTicks);
    }
}
