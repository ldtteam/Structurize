package com.ldtteam.blockout;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import com.mojang.math.Matrix4f;

/**
 * Helpful util methods when using Matrixes
 */
public class MatrixUtils
{
    /**
     * Private constructor to hide the public one.
     */
    private MatrixUtils()
    {
    }

    /**
     * @return last matrix X translate value
     */
    public static int getLastMatrixTranslateXasInt(final PoseStack matrixStack)
    {
        return Mth.floor(getLastMatrixTranslateX(matrixStack));
    }

    /**
     * @return last matrix Y translate value
     */
    public static int getLastMatrixTranslateYasInt(final PoseStack matrixStack)
    {
        return Mth.floor(getLastMatrixTranslateY(matrixStack));
    }

    /**
     * @return last matrix Z translate value
     */
    public static int getLastMatrixTranslateZasInt(final PoseStack matrixStack)
    {
        return Mth.floor(getLastMatrixTranslateZ(matrixStack));
    }

    /**
     * @return last matrix X translate value
     */
    public static float getLastMatrixTranslateX(final PoseStack matrixStack)
    {
        return getMatrixTranslateX(matrixStack.last().pose());
    }

    /**
     * @return last matrix Y translate value
     */
    public static float getLastMatrixTranslateY(final PoseStack matrixStack)
    {
        return getMatrixTranslateY(matrixStack.last().pose());
    }

    /**
     * @return last matrix Z translate value
     */
    public static float getLastMatrixTranslateZ(final PoseStack matrixStack)
    {
        return getMatrixTranslateZ(matrixStack.last().pose());
    }

    /**
     * @return matrix X translate value
     */
    public static float getMatrixTranslateX(final Matrix4f matrix)
    {
        return matrix.m03;
    }

    /**
     * @return matrix Y translate value
     */
    public static float getMatrixTranslateY(final Matrix4f matrix)
    {
        return matrix.m13;
    }

    /**
     * @return matrix Z translate value
     */
    public static float getMatrixTranslateZ(final Matrix4f matrix)
    {
        return matrix.m23;
    }
}
