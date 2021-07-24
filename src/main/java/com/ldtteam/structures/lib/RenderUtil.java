package com.ldtteam.structures.lib;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import com.mojang.math.Vector3f;
import org.jetbrains.annotations.NotNull;

public final class RenderUtil
{

    public static final float QUARTER = 90F;
    public static final float HALF = 180F;

    private RenderUtil()
    {
        throw new IllegalArgumentException("Utility Class");
    }

    public static void applyRotationToYAxis(@NotNull final Rotation rotation, final PoseStack stack)
    {
        //stack.scale(0.5F, 0F, 0.5F);

        float angle;
        switch (rotation)
        {
            case NONE:
                angle = 0F;
                break;
            case CLOCKWISE_90:
                angle = -QUARTER;
                break;
            case CLOCKWISE_180:
                angle = -HALF;
                break;
            case COUNTERCLOCKWISE_90:
                angle = QUARTER;
                break;
            default:
                angle = 0F;
                break;
        }

        stack.mulPose(Vector3f.YP.rotationDegrees(angle));

        //stack.scale(-0.5F, 0F, -0.5F);
    }

    public static void applyMirror(@NotNull final Mirror mirror, @NotNull final BlockPos appliedPrimaryBlockOff, final PoseStack stack)
    {
        switch (mirror)
        {
            case NONE:
                stack.scale(1, 1, 1);
                break;
            case FRONT_BACK:
                stack.translate((2 * appliedPrimaryBlockOff.getX()) + 1, 0, 0);
                stack.scale(-1, 1, 1);
                break;
            case LEFT_RIGHT:
                stack.translate(0, 0, (2 * appliedPrimaryBlockOff.getZ()) + 1);
                stack.scale(1, 1, -1);
                break;
            default:
                //Should never occur.
                break;
        }
    }
}