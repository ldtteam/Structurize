package com.ldtteam.structures.client;

import java.util.List;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class StructureClientHandler
{

    private StructureClientHandler()
    {
        throw new IllegalArgumentException("Utility class");
    }

    @Deprecated // INTERNAL USE ONLY
    public static void renderStructure(final Blueprint blueprint, final float partialTicks, final BlockPos pos, final PoseStack stack)
    {
        BlueprintHandler.getInstance().draw(blueprint, pos, stack, partialTicks);
    }

    /**
     * Renders blueprint at single position.
     *
     * @param blueprint what to render
     * @param pos       where to render
     */
    public static void renderStructureAtPos(final Blueprint blueprint, final float partialTicks, final BlockPos pos, final PoseStack stack)
    {
        OptifineCompat.getInstance().preBlueprintDraw();
        renderStructure(blueprint, partialTicks, pos, stack);
        OptifineCompat.getInstance().postBlueprintDraw();
    }

    @Deprecated // INTERNAL USE ONLY
    public static void renderStructure(final Blueprint blueprint, final float partialTicks, final List<BlockPos> points, final PoseStack stack)
    {
        BlueprintHandler.getInstance().drawAtListOfPositions(blueprint, points, stack, partialTicks);
    }

    /**
     * Renders blueprint at list of positions.
     * @param blueprint the blueprint to render.
     * @param partialTicks the partial ticks.
     * @param points the list of points.
     * @param stack the matrix stack to render it in.
     */
    public static void renderStructureAtPosList(final Blueprint blueprint, final float partialTicks, final List<BlockPos> points, final PoseStack stack)
    {
        OptifineCompat.getInstance().preBlueprintDraw();
        renderStructure(blueprint, partialTicks, points, stack);
        OptifineCompat.getInstance().postBlueprintDraw();
    }
}
