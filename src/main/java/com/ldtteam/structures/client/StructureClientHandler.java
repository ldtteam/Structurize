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
    public static void renderStructure(@NotNull final Blueprint blueprint, final float partialTicks, final BlockPos pos, final MatrixStack stack)
    {
        BlueprintHandler.getInstance().draw(blueprint, pos, stack, partialTicks);
    }

    public static void renderStructureAtPos(@NotNull final Blueprint blueprint, final float partialTicks, final BlockPos pos, final MatrixStack stack)
    {
        OptifineCompat.getInstance().preBlueprintDraw();
        renderStructure(blueprint, partialTicks, pos, stack);
        OptifineCompat.getInstance().postBlueprintDraw();
    }

    @Deprecated // INTERNAL USE ONLY
    public static void renderStructure(@NotNull final Blueprint blueprint, final float partialTicks, final List<BlockPos> points, final MatrixStack stack)
    {
        BlueprintHandler.getInstance().drawAtListOfPositions(blueprint, points, stack, partialTicks);
    }

    public static void renderStructureAtPosList(@NotNull final Blueprint blueprint, final float partialTicks, final List<BlockPos> points, final MatrixStack stack)
    {
        OptifineCompat.getInstance().preBlueprintDraw();
        renderStructure(blueprint, partialTicks, points, stack);
        OptifineCompat.getInstance().postBlueprintDraw();
    }
}