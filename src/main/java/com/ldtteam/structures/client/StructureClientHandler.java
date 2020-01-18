package com.ldtteam.structures.client;

import com.ldtteam.structures.helpers.Structure;
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

    public static void renderStructure(@NotNull final Structure structure, final float partialTicks, final BlockPos pos, final MatrixStack stack)
    {
        renderStructure(structure, Minecraft.getInstance().player, partialTicks, pos, stack);
    }

    public static void renderStructure(@NotNull final Structure structure, @Nullable final Entity perspectiveEntity, final float partialTicks, final BlockPos pos, final MatrixStack stack)
    {
        if (perspectiveEntity != null)
        {
            BlueprintHandler.getInstance().draw(structure.getBluePrint(), structure.getSettings().getRotation(), structure.getSettings().getMirror(), pos, stack, partialTicks);
        }
    }
}
