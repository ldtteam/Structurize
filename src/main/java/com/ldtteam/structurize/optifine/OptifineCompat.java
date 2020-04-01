package com.ldtteam.structurize.optifine;

import com.ldtteam.structurize.api.util.Log;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

        calcNormalForLayerMethod = sVertexBuilderClass.getMethod("calcNormalChunkLayer", BufferBuilder.class);
        calcNormalForLayerMethod.setAccessible(true);

        setupArrayPointersVboMethod = shaderRenderClass.getMethod("setupArrayPointersVbo");
        setupArrayPointersVboMethod.setAccessible(true);

        preRenderChunkLayerMethod = shaderRenderClass.getMethod("preRenderChunkLayer", RenderType.class);
        preRenderChunkLayerMethod.setAccessible(true);

        postRenderChunkLayerMethod = shaderRenderClass.getMethod("postRenderChunkLayer", RenderType.class);
        postRenderChunkLayerMethod.setAccessible(true);

        endTerrainMethod = shaderRenderClass.getMethod("endTerrain");
        endTerrainMethod.setAccessible(true);

        isShadowPassField = shadersClass.getField("isShadowPass");
        isShadowPassField.setAccessible(true);

        isRenderingWorldField = shadersClass.getField("isRenderingWorld");
        isRenderingWorldField.setAccessible(true);
    }

    /**
     * Call to setup the shader in Optifine.
     * Checks if the compat is enabled or not.
     */
    public void preBlueprintDraw()
    {
        tryRun(() -> {
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                currentShadowPassFieldValue = isShadowPassField.getBoolean(null);
                isShadowPassField.set(null, false);

                currentIsRenderingWorldFieldValue = isRenderingWorldField.getBoolean(null);
                isRenderingWorldField.set(null, true);
            }
        });
    }

    /**
     * Call to disable the shader
     * Checks if the compat is enabled or not.
     */
    public void postBlueprintDraw()
    {
        tryRun(() -> {
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                endTerrainMethod.invoke(null);

                isShadowPassField.set(null, currentShadowPassFieldValue);
                isRenderingWorldField.set(null, currentIsRenderingWorldFieldValue);
            }
        });
    }

    /**
     * Setups layer rendering.
     *
     * @param layer block layer rendertype
     */
    public void preLayerDraw(final RenderType layer)
    {
        tryRun(() -> {
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                preRenderChunkLayerMethod.invoke(null, layer);
            }
        });
    }

    /**
     * Finishes layer rendering.
     *
     * @param layer block layer rendertype
     */
    public void postLayerDraw(final RenderType layer)
    {
        tryRun(() -> {
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                postRenderChunkLayerMethod.invoke(null, layer);
            }
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
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                calcNormalForLayerMethod.invoke(null, bufferBuilder);
            }
        });
    }

    /**
     * Called to setup the pointers in the arrays.
     */
    public void setupArrayPointers()
    {
        tryRun(() -> {
            if ((Boolean) isShadersEnabledMethod.invoke(null))
            {
                setupArrayPointersVboMethod.invoke(null);
            }
        });
    }

    /**
     * Checks if the compat is enabled or not. Then tries to run supplied code runnable.
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
