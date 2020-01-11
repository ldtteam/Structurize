package com.ldtteam.structures.client;

import com.ldtteam.structures.helpers.Structure;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
            final Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
            Vec3d vec = new Vec3d(pos).subtract(projectedView);

            final Vector3d renderOffset = new Vector3d(vec.x, vec.y, vec.z);
            BlueprintHandler.getInstance().draw(structure.getBluePrint(), structure.getSettings().getRotation(), structure.getSettings().getMirror(), renderOffset, stack, partialTicks);
        }
    }
}
