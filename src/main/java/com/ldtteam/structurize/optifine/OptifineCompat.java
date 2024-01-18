package com.ldtteam.structurize.optifine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The optifine compat layer.
 * Allows shaders to work somewhat.
 */
public class OptifineCompat
{
    private static OptifineCompat ourInstance = new OptifineCompat();

    public static OptifineCompat getInstance()
    {
        return ourInstance;
    }

    private OptifineCompat()
    {
    }

    public boolean isOptifineEnabled()
    {
        return false;
    }

    /**
     * Initializes the compat layer.
     * Makes sure that all relevant classes are available as well as all required methods.
     * Will disable compat if either a class is missing, or a method is missing.
     * This ensures that if, optifines structure changes we do not crash and just disable the compat.
     */
    public void intialize()
    {
    }

    /**
     * Performs the reflective access to the Optifine related methods.
     *
     * @throws ClassNotFoundException Thrown when a optifine class is missing.
     * @throws NoSuchMethodException  Thrown when a optifine method is missing.
     * @throws NoSuchFieldException   Thrown when a optifine field is missing.
     */
    private void setupReflectedMethodReferences() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException
    {
    }

    /**
     * Sets up optifine fog settings.
     */
    public void setupFog()
    {
    }

    /**
     * Resets optifine fog settings.
     */
    public void resetFog()
    {
    }

    /**
     * Called to handle the buffer information for optifine.
     * Calculates the normals of the faces.
     *
     * @param bufferBuilder The bufferBuilder that is about to be uploaded to the GPU.
     */
    public void beforeBuilderUpload(final BufferBuilder bufferBuilder)
    {
    }

    /**
     * Call to setup the shader in Optifine.
     * Checks if the compat is enabled or not.
     */
    public void preBlueprintDraw()
    {
    }

    /**
     * Setups layer rendering.
     *
     * @param layer block layer rendertype
     */
    public void preLayerDraw(final RenderType layer, final Matrix4f mvMatrix)
    {
    }

    /**
     * @return true if any shader program is active
     */
    public boolean isShaderProgramActive()
    {
        return false;
    }

    /**
     * Sets optifine version of shaderinstance.CHUNK_OFFSET
     * 
     * @param x chunk blockpos x offset
     * @param y chunk blockpos y offset
     * @param z chunk blockpos z offset
     */
    public void setUniformChunkOffset(final float x, final float y, final float z)
    {
    }

    /**
     * Called to setup the pointers in the arrays.
     */
    public void setupArrayPointers()
    {
    }

    /**
     * Finishes layer rendering.
     *
     * @param layer block layer rendertype
     */
    public void postLayerDraw(final RenderType layer)
    {
    }

    public void endTerrainBeginEntities()
    {
    }

    public void preRenderEntity(final Entity entity)
    {
    }

    public void endEntitiesBeginBlockEntities()
    {
    }

    public void preRenderBlockEntity(final BlockEntity blockEntity)
    {
    }

    public void endBlockEntitiesBeginDebug(final RenderBuffers renderBuffers)
    {
    }

    public void endDebugPreWaterBeginWater()
    {
    }

    public void endWater()
    {
    }

    /**
     * Call to disable the shader
     * Checks if the compat is enabled or not.
     */
    public void postBlueprintDraw()
    {
    }

    /**
     * Checks if the compat is enabled and if shaders are enabled. Then tries to run supplied Runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Runnable to run
     */
    private void tryRunIfShadersEnabled(final ReflectionRunnable code)
    {
    }

    /**
     * Checks if the compat is enabled. Then tries to run supplied Runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Runnable to run
     */
    private void tryRun(final ReflectionRunnable code)
    {
    }

    /**
     * Checks if the compat is enabled and if shaders are enabled. Then tries to run supplied Supplier.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Supplier to run
     */
    private <T> T trySupplyIfShadersEnabled(final ReflectionSupplier<T> code, final T defaultValue)
    {
        return defaultValue;
    }

    /**
     * Checks if the compat is enabled. Then tries to run supplied Supplier.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Supplier to run
     */
    private <T> T trySupply(final ReflectionSupplier<T> code, final T defaultValue)
    {
        return defaultValue;
    }

    @FunctionalInterface
    private interface ReflectionRunnable
    {
        void run() throws ReflectiveOperationException;
    }

    @FunctionalInterface
    private interface ReflectionSupplier<T>
    {
        T get() throws ReflectiveOperationException;
    }


    @FunctionalInterface
    private interface TriFloatReflectionConsumer
    {
        void invoke(float f, float g, float h) throws ReflectiveOperationException;
    }
}
