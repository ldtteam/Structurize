package com.ldtteam.structurize.optifine;

import com.ldtteam.structurize.api.util.Log;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The optifine compat layer.
 * Allows shaders to work somewhat.
 */
public class OptifineCompat
{
    private static final String DISABLE_REASON = "waiting for optifine to catch up with vanilla";

    private static OptifineCompat ourInstance = new OptifineCompat();

    private Method isFogOffMethod;
    private Method setFogAllowedMethod;
    private Method isShadersEnabledMethod;
    private Method calcNormalForLayerMethod;
    private Method setupArrayPointersVboMethod;
    private Method preRenderChunkLayerMethod;
    private Method setModelViewMatrixMethod;
    private Method setProjectionMatrixMethod;
    private Method setTextureMatrixMethod;
    private Method setColorModulatorMethod;
    private TriFloatReflectionConsumer setUniformChunkOffsetValueMethod;
    private Method postRenderChunkLayerMethod;
    private Method endTerrainMethod;
    private Method beginEntitiesMethod;
    private Method nextEntityMethod;
    private Method endEntitiesMethod;
    private Method beginBlockEntitiesMethod;
    private Method nextBlockEntityMethod;
    private Method endBlockEntitiesMethod;
    private Method flushRenderBuffersMethod;
    private Method beginDebug;
    private Method endDebug;
    private Method preWaterMethod;
    private Method beginWaterMethod;
    private Method endWaterMethod;

    private boolean currentShadowPassFieldValue = false;
    private Field isShadowPassField;

    private boolean currentIsRenderingWorldFieldValue = false;
    private Field isRenderingWorldField;

    private Field fogStandardField;
    private Field activeProgramIdField;

    public static OptifineCompat getInstance()
    {
        return ourInstance;
    }

    private boolean enableOptifine = false;

    private OptifineCompat()
    {
    }

    public boolean isOptifineEnabled()
    {
        return enableOptifine;
    }

    /**
     * Initializes the compat layer.
     * Makes sure that all relevant classes are available as well as all required methods.
     * Will disable compat if either a class is missing, or a method is missing.
     * This ensures that if, optifines structure changes we do not crash and just disable the compat.
     */
    public void intialize()
    {
        if (DISABLE_REASON != null && !DISABLE_REASON.isBlank())
        {
            Log.getLogger().info("Optifine compat disabled. Reason: " + DISABLE_REASON);
            return;
        }

        try
        {
            setupReflectedMethodReferences();

            Log.getLogger().info("Optifine found. Enabling compat.");
            enableOptifine = true;
        }
        catch (final ClassNotFoundException e)
        {
            Log.getLogger().info("Optifine not found. Disabling compat.");
            enableOptifine = false;
        }
        catch (final NoSuchMethodException e)
        {
            Log.getLogger().error("Optifine found. But could not access related methods.", e);
            enableOptifine = false;
        }
        catch (final NoSuchFieldException e)
        {
            Log.getLogger().error("Optifine found. But could not access related fields", e);
            enableOptifine = false;
        }
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
        final Class<?> configClass = Class.forName("net.optifine.Config");
        final Class<?> shaderRenderClass = Class.forName("net.optifine.shaders.ShadersRender");
        final Class<?> sVertexBuilderClass = Class.forName("net.optifine.shaders.SVertexBuilder");
        final Class<?> shadersClass = Class.forName("net.optifine.shaders.Shaders");
        final Class<?> shaderUniform3fClass = Class.forName("net.optifine.shaders.uniform.ShaderUniform3f");

        final Class<RenderSystem> renderSystemClass = RenderSystem.class;
        final Class<FogRenderer> fogRendererClass = FogRenderer.class;
        final Class<MultiBufferSource> multiBufferSourceClass = MultiBufferSource.class;

        isFogOffMethod = configClass.getMethod("isFogOff");
        isFogOffMethod.setAccessible(true); 

        fogStandardField = fogRendererClass.getField("fogStandard");
        fogStandardField.setAccessible(true);
        
        setFogAllowedMethod = renderSystemClass.getMethod("setFogAllowed", boolean.class);
        setFogAllowedMethod.setAccessible(true);

        isShadersEnabledMethod = configClass.getMethod("isShaders");
        isShadersEnabledMethod.setAccessible(true);

        isShadowPassField = shadersClass.getField("isShadowPass");
        isShadowPassField.setAccessible(true);

        isRenderingWorldField = shadersClass.getField("isRenderingWorld");
        isRenderingWorldField.setAccessible(true);

        calcNormalForLayerMethod = sVertexBuilderClass.getMethod("calcNormalChunkLayer", BufferBuilder.class);
        calcNormalForLayerMethod.setAccessible(true);

        preRenderChunkLayerMethod = shaderRenderClass.getMethod("preRenderChunkLayer", RenderType.class);
        preRenderChunkLayerMethod.setAccessible(true);

        setModelViewMatrixMethod = shadersClass.getMethod("setModelViewMatrix", Matrix4f.class);
        setModelViewMatrixMethod.setAccessible(true);

        setProjectionMatrixMethod = shadersClass.getMethod("setProjectionMatrix", Matrix4f.class);
        setProjectionMatrixMethod.setAccessible(true);

        setTextureMatrixMethod = shadersClass.getMethod("setTextureMatrix", Matrix4f.class);
        setTextureMatrixMethod.setAccessible(true);

        setColorModulatorMethod = shadersClass.getMethod("setColorModulator", float[].class);
        setColorModulatorMethod.setAccessible(true);

        activeProgramIdField = shadersClass.getField("activeProgramID");
        activeProgramIdField.setAccessible(true);

        final Field uniformChunkOffsetField = shadersClass.getField("uniform_chunkOffset");
        uniformChunkOffsetField.setAccessible(true);

        final Method setValueMethod = shaderUniform3fClass.getMethod("setValue", float.class, float.class, float.class);
        setValueMethod.setAccessible(true);

        setUniformChunkOffsetValueMethod = (x, y, z) -> setValueMethod.invoke(uniformChunkOffsetField.get(null), x, y, z);

        setupArrayPointersVboMethod = shaderRenderClass.getMethod("setupArrayPointersVbo");
        setupArrayPointersVboMethod.setAccessible(true);

        postRenderChunkLayerMethod = shaderRenderClass.getMethod("postRenderChunkLayer", RenderType.class);
        postRenderChunkLayerMethod.setAccessible(true);

        endTerrainMethod = shaderRenderClass.getMethod("endTerrain");
        endTerrainMethod.setAccessible(true);

        beginEntitiesMethod = shadersClass.getMethod("beginEntities");
        beginEntitiesMethod.setAccessible(true);

        nextEntityMethod = shadersClass.getMethod("nextEntity", Entity.class);
        nextEntityMethod.setAccessible(true);

        endEntitiesMethod = shadersClass.getMethod("endEntities");
        endEntitiesMethod.setAccessible(true);

        beginBlockEntitiesMethod = shadersClass.getMethod("beginBlockEntities");
        beginBlockEntitiesMethod.setAccessible(true);

        nextBlockEntityMethod = shadersClass.getMethod("nextBlockEntity", BlockEntity.class);
        nextBlockEntityMethod.setAccessible(true);

        endBlockEntitiesMethod = shadersClass.getMethod("endBlockEntities");
        endBlockEntitiesMethod.setAccessible(true);

        flushRenderBuffersMethod = multiBufferSourceClass.getMethod("flushRenderBuffers");
        flushRenderBuffersMethod.setAccessible(true);

        beginDebug = shaderRenderClass.getMethod("beginDebug");
        beginDebug.setAccessible(true);

        endDebug = shaderRenderClass.getMethod("endDebug");
        endDebug.setAccessible(true);

        preWaterMethod = shadersClass.getMethod("preWater");
        preWaterMethod.setAccessible(true);

        beginWaterMethod = shadersClass.getMethod("beginWater");
        beginWaterMethod.setAccessible(true);

        endWaterMethod = shadersClass.getMethod("endWater");
        endWaterMethod.setAccessible(true);
    }

    /**
     * Sets up optifine fog settings.
     */
    public void setupFog()
    {
        tryRun(() -> {
            if ((Boolean) isFogOffMethod.invoke(null) && fogStandardField.getBoolean(null))
            {
                setFogAllowedMethod.invoke(null, Boolean.FALSE);
            }
        });
    }

    /**
     * Resets optifine fog settings.
     */
    public void resetFog()
    {
        tryRun(() -> {
            setFogAllowedMethod.invoke(null, Boolean.TRUE);
        });
    }

    /**
     * Called to handle the buffer information for optifine.
     * Calculates the normals of the faces.
     *
     * @param bufferBuilder The bufferBuilder that is about to be uploaded to the GPU.
     */
    public void beforeBuilderUpload(final BufferBuilder bufferBuilder)
    {
        tryRun(() -> {
            calcNormalForLayerMethod.invoke(null, bufferBuilder);
        });
    }

    /**
     * Call to setup the shader in Optifine.
     * Checks if the compat is enabled or not.
     */
    public void preBlueprintDraw()
    {
        tryRunIfShadersEnabled(() -> {
            currentShadowPassFieldValue = isShadowPassField.getBoolean(null);
            isShadowPassField.set(null, false);

            currentIsRenderingWorldFieldValue = isRenderingWorldField.getBoolean(null);
            isRenderingWorldField.set(null, true);
        });
    }

    /**
     * Setups layer rendering.
     *
     * @param layer block layer rendertype
     */
    public void preLayerDraw(final RenderType layer, final Matrix4f mvMatrix)
    {
        tryRunIfShadersEnabled(() -> {
            preRenderChunkLayerMethod.invoke(null, layer);
            setModelViewMatrixMethod.invoke(null, mvMatrix);
            setProjectionMatrixMethod.invoke(null, RenderSystem.getProjectionMatrix());
            setTextureMatrixMethod.invoke(null, RenderSystem.getTextureMatrix());
            setColorModulatorMethod.invoke(null, RenderSystem.getShaderColor());
        });
    }

    /**
     * @return true if any shader program is active
     */
    public boolean isShaderProgramActive()
    {
        return trySupplyIfShadersEnabled(() -> activeProgramIdField.getInt(null) > 0, false);
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
        tryRunIfShadersEnabled(() -> {
            setUniformChunkOffsetValueMethod.invoke(x, y, z);
        });
    }

    /**
     * Called to setup the pointers in the arrays.
     */
    public void setupArrayPointers()
    {
        tryRunIfShadersEnabled(() -> {
            setupArrayPointersVboMethod.invoke(null);
        });
    }

    /**
     * Finishes layer rendering.
     *
     * @param layer block layer rendertype
     */
    public void postLayerDraw(final RenderType layer)
    {
        tryRunIfShadersEnabled(() -> {
            postRenderChunkLayerMethod.invoke(null, layer);
        });
    }

    public void endTerrainBeginEntities()
    {
        tryRunIfShadersEnabled(() -> {
            endTerrainMethod.invoke(null);
            beginEntitiesMethod.invoke(null);
        });
    }

    public void preRenderEntity(final Entity entity)
    {
        tryRunIfShadersEnabled(() -> {
            nextEntityMethod.invoke(null, entity);
        });
    }

    public void endEntitiesBeginBlockEntities()
    {
        tryRunIfShadersEnabled(() -> {
            endEntitiesMethod.invoke(null);
            beginBlockEntitiesMethod.invoke(null);
        });
    }

    public void preRenderBlockEntity(final BlockEntity blockEntity)
    {
        tryRunIfShadersEnabled(() -> {
            nextBlockEntityMethod.invoke(null, blockEntity);
        });
    }

    public void endBlockEntitiesBeginDebug(final RenderBuffers renderBuffers)
    {
        tryRunIfShadersEnabled(() -> {
            endBlockEntitiesMethod.invoke(null);
        });
        tryRun(() -> {
            flushRenderBuffersMethod.invoke(renderBuffers.bufferSource());
            flushRenderBuffersMethod.invoke(renderBuffers.crumblingBufferSource());
        });
        tryRunIfShadersEnabled(() -> {
            beginDebug.invoke(null);
        });
    }

    public void endDebugPreWaterBeginWater()
    {
        tryRunIfShadersEnabled(() -> {
            endDebug.invoke(null);
            preWaterMethod.invoke(null);
            beginWaterMethod.invoke(null);
        });
    }

    public void endWater()
    {
        tryRunIfShadersEnabled(() -> {
            endWaterMethod.invoke(null);
        });
    }

    /**
     * Call to disable the shader
     * Checks if the compat is enabled or not.
     */
    public void postBlueprintDraw()
    {
        tryRunIfShadersEnabled(() -> {
            isShadowPassField.set(null, currentShadowPassFieldValue);
            isRenderingWorldField.set(null, currentIsRenderingWorldFieldValue);
        });
    }

    /**
     * Checks if the compat is enabled and if shaders are enabled. Then tries to run supplied Runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Runnable to run
     */
    private void tryRunIfShadersEnabled(final ReflectionRunnable code)
    {
        tryRun(() -> {
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                code.run();
            }
        });
    }

    /**
     * Checks if the compat is enabled. Then tries to run supplied Runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Runnable to run
     */
    private void tryRun(final ReflectionRunnable code)
    {
        if (!enableOptifine)
        {
            return;
        }

        try
        {
            code.run();
        }
        catch (final ReflectiveOperationException e)
        {
            Log.getLogger().error("Failed to access Optifine related rendering things.", e);
            Log.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        }
    }

    /**
     * Checks if the compat is enabled and if shaders are enabled. Then tries to run supplied Supplier.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Supplier to run
     */
    private <T> T trySupplyIfShadersEnabled(final ReflectionSupplier<T> code, final T defaultValue)
    {
        return trySupply(() -> (Boolean) isShadersEnabledMethod.invoke(null) ? code.get() : defaultValue, defaultValue);
    }

    /**
     * Checks if the compat is enabled. Then tries to run supplied Supplier.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code Supplier to run
     */
    private <T> T trySupply(final ReflectionSupplier<T> code, final T defaultValue)
    {
        if (!enableOptifine)
        {
            return defaultValue;
        }

        try
        {
            return code.get();
        }
        catch (final ReflectiveOperationException e)
        {
            Log.getLogger().error("Failed to access Optifine related rendering things.", e);
            Log.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        }
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
