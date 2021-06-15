package com.ldtteam.blockout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

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
    public static int getLastMatrixTranslateXasInt(final MatrixStack matrixStack)
    {
        return MathHelper.floor(getLastMatrixTranslateX(matrixStack));
    }

    /**
     * @return last matrix Y translate value
     */
    public static int getLastMatrixTranslateYasInt(final MatrixStack matrixStack)
    {
        return MathHelper.floor(getLastMatrixTranslateY(matrixStack));
    }

    /**
     * @return last matrix Z translate value
     */
    public static int getLastMatrixTranslateZasInt(final MatrixStack matrixStack)
    {
        return MathHelper.floor(getLastMatrixTranslateZ(matrixStack));
    }

    /**
     * @return last matrix X translate value
     */
    public static float getLastMatrixTranslateX(final MatrixStack matrixStack)
    {
        return getMatrixTranslateX(matrixStack.last().pose());
    }

    /**
     * @return last matrix Y translate value
     */
    public static float getLastMatrixTranslateY(final MatrixStack matrixStack)
    {
        return getMatrixTranslateY(matrixStack.last().pose());
    }

    /**
     * @return last matrix Z translate value
     */
    public static float getLastMatrixTranslateZ(final MatrixStack matrixStack)
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
