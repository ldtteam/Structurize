package com.ldtteam.structures.client;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StructureClientHandler
{

    private StructureClientHandler()
    {
        throw new IllegalArgumentException("Utility class");
    }

    public static void renderStructure(@NotNull final Blueprint blueprint, final float partialTicks, final BlockPos pos, final MatrixStack stack)
    {
        renderStructure(blueprint, Minecraft.getInstance().player, partialTicks, pos, stack);
    }

    public static void renderStructure(@NotNull final Blueprint blueprint, @Nullable final Entity perspectiveEntity, final float partialTicks, final BlockPos pos, final MatrixStack stack)
    {
        if (perspectiveEntity != null)
        {
            BlueprintHandler.getInstance().draw(blueprint, pos, stack, partialTicks);
        }
    }
}
