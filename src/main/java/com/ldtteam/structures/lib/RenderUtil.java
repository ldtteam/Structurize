package com.ldtteam.structures.lib;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public final class RenderUtil
{

    public static final float QUARTER = 90F;
    public static final float HALF = 180F;

    private RenderUtil()
    {
        throw new IllegalArgumentException("Utility Class");
    }

    public static void applyRotationToYAxis(@NotNull final Rotation rotation)
    {
        RenderSystem.translated(0.5F, 0F, 0.5F);

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

        RenderSystem.rotatef(angle, 0, 1, 0);

        RenderSystem.translated(-0.5F, 0F, -0.5F);
    }

    public static void applyMirror(@NotNull final Mirror mirror, @NotNull final BlockPos appliedPrimaryBlockOff)
    {
        switch (mirror)
        {
            case NONE:
                RenderSystem.scaled(1, 1, 1);
                break;
            case FRONT_BACK:
                RenderSystem.translated((2 * appliedPrimaryBlockOff.getX()) + 1, 0, 0);
                RenderSystem.scaled(-1, 1, 1);
                break;
            case LEFT_RIGHT:
                RenderSystem.translated(0, 0, (2 * appliedPrimaryBlockOff.getZ()) + 1);
                RenderSystem.scaled(1, 1, -1);
                break;
            default:
                //Should never occur.
                break;
        }
    }
}