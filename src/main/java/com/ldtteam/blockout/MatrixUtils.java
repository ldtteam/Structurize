package com.ldtteam.blockout;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
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
     * Sets up Minecraft internal z levels to make sure we are starting at our z level.
     *
     * @param matrixStack stack given by Minecraft when rendering guis
     * @param gui         root gui to set z blit offset for
     */
    public static void setupZLevelFromMatrixStack(final MatrixStack matrixStack, final AbstractGui gui)
    {
        final int z = getLastMatrixTranslateZasInt(matrixStack);

        Minecraft.getInstance().getItemRenderer().zLevel = z;
        if (gui != null)
        {
            gui.setBlitOffset(z);
        }
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
        return getMatrixTranslateX(matrixStack.getLast().getMatrix());
    }

    /**
     * @return last matrix Y translate value
     */
    public static float getLastMatrixTranslateY(final MatrixStack matrixStack)
    {
        return getMatrixTranslateY(matrixStack.getLast().getMatrix());
    }

    /**
     * @return last matrix Z translate value
     */
    public static float getLastMatrixTranslateZ(final MatrixStack matrixStack)
    {
        return getMatrixTranslateZ(matrixStack.getLast().getMatrix());
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
