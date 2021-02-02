package com.ldtteam.structures.client;

import java.util.List;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.optifine.OptifineCompat;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class StructureClientHandler
{

    private StructureClientHandler()
    {
        throw new IllegalArgumentException("Utility class");
    }

    @Deprecated // INTERNAL USE ONLY
    public static void renderStructure(@NotNull final Blueprint blueprint, final float partialTicks, final BlockPos pos, final MatrixStack stack, final boolean dynamic)
    {
        BlueprintHandler.getInstance().draw(blueprint, pos, stack, partialTicks, dynamic);
    }

    /**
     * Renders blueprint at single position.
     *
     * @param blueprint what to render
     * @param pos       where to render
     * @param partialTicks the partial ticks.
     * @param stack the matrix stack.
     * @param dynamic if dynamic (might change rotation and mirror) or static (doesn't change).
     */
    public static void renderStructureAtPos(@NotNull final Blueprint blueprint, final float partialTicks, final BlockPos pos, final MatrixStack stack, final boolean dynamic)
    {
        OptifineCompat.getInstance().preBlueprintDraw();
        renderStructure(blueprint, partialTicks, pos, stack, dynamic);
        OptifineCompat.getInstance().postBlueprintDraw();
    }

    @Deprecated // INTERNAL USE ONLY
    public static void renderStructure(@NotNull final Blueprint blueprint, final float partialTicks, final List<BlockPos> points, final MatrixStack stack, final boolean dynamic)
    {
        BlueprintHandler.getInstance().drawAtListOfPositions(blueprint, points, stack, partialTicks, dynamic);
    }

    /**
     * Renders blueprint at list of positions.
     *
     * @param partialTicks the partial ticks.
     * @param stack the matrix stack.
     * @param dynamic if dynamic (might change rotation and mirror) or static (doesn't change).
     * @param points the points to render.
     * @param blueprint the blueprint to render.
     */
    public static void renderStructureAtPosList(@NotNull final Blueprint blueprint, final float partialTicks, final List<BlockPos> points, final MatrixStack stack, final boolean dynamic)
    {
        OptifineCompat.getInstance().preBlueprintDraw();
        renderStructure(blueprint, partialTicks, points, stack, dynamic);
        OptifineCompat.getInstance().postBlueprintDraw();
    }
}
