package com.ldtteam.structurize.optifine;

import com.ldtteam.structurize.api.util.Log;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The optifine compat layer.
 * Allows shaders to work somewhat.
 */
public class OptifineCompat
{
    private static OptifineCompat ourInstance = new OptifineCompat();

    private Method isShadersEnabledMethod;
    private Method calcNormalForLayerMethod;
    private Method setupArrayPointersVboMethod;
    private Method preRenderChunkLayerMethod;
    private Method postRenderChunkLayerMethod;
    private Method endTerrainMethod;
    private Method beginEntitiesMethod;
    private Method nextEntityMethod;
    private Method endEntitiesMethod;
    private Method beginBlockEntitiesMethod;
    private Method nextBlockEntityMethod;
    private Method endBlockEntitiesMethod;
    private Method preWaterMethod;
    private Method beginWaterMethod;
    private Method endWaterMethod;

    private boolean currentShadowPassFieldValue = false;
    private Field isShadowPassField;

    private boolean currentIsRenderingWorldFieldValue = false;
    private Field isRenderingWorldField;

    public static OptifineCompat getInstance()
    {
        return ourInstance;
    }

    private boolean enableOptifine = false;

    private OptifineCompat()
    {
    }

    /**
     * Initializes the compat layer.
     * Makes sure that all relevant classes are available as well as all required methods.
     * Will disable compat if either a class is missing, or a method is missing.
     * This ensures that if, optifines structure changes we do not crash and just disable the compat.
     */
    public void intialize()
    {
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

        nextBlockEntityMethod = shadersClass.getMethod("nextBlockEntity", TileEntity.class);
        nextBlockEntityMethod.setAccessible(true);

        endBlockEntitiesMethod = shadersClass.getMethod("endBlockEntities");
        endBlockEntitiesMethod.setAccessible(true);

        preWaterMethod = shadersClass.getMethod("preWater");
        preWaterMethod.setAccessible(true);

        beginWaterMethod = shadersClass.getMethod("beginWater");
        beginWaterMethod.setAccessible(true);

        endWaterMethod = shadersClass.getMethod("endWater");
        endWaterMethod.setAccessible(true);
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
    public void preLayerDraw(final RenderType layer)
    {
        tryRunIfShadersEnabled(() -> {
            preRenderChunkLayerMethod.invoke(null, layer);
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

    public void preRenderBlockEntity(final TileEntity blockEntity)
    {
        tryRunIfShadersEnabled(() -> {
            nextBlockEntityMethod.invoke(null, blockEntity);
        });
    }

    public void endBlockEntitiesPreWaterBeginWater()
    {
        tryRunIfShadersEnabled(() -> {
            endBlockEntitiesMethod.invoke(null);
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
     * Checks if the compat is enabled and if shaders are disabled. Then tries to run supplied code runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code runnable to run
     */
    /*
    private void tryRunIfShadersDisabled(final ReflectionRunnable code)
    {
        tryRun(() -> {
            if (!(Boolean) isShadersEnabledMethod.invoke(null))
            {
                code.run();
            }
        });
    }
    */

    /**
     * Checks if the compat is enabled and if shaders are enabled. Then tries to run supplied code runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code runnable to run
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
     * Checks if the compat is enabled. Then tries to run supplied code runnable.
     * Catches any ReflectiveOperationException so we can disable compat layer securely.
     *
     * @param code runnable to run
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

    @FunctionalInterface
    private interface ReflectionRunnable
    {
        /**
         * @throws ReflectiveOperationException if any reflection operation failed for any reason
         * @see Runnable#run()
         */
        void run() throws ReflectiveOperationException;
    }
}
