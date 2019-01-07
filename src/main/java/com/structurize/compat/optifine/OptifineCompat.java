package com.structurize.compat.optifine;

import com.structurize.coremod.Structurize;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptifineCompat {
    private static OptifineCompat ourInstance = new OptifineCompat();
    private Method isShadersEnabledMethod;
    private Method preRenderChunkLayerMethod;
    private Method postRenderChunkLayerMethod;
    private Method setupArrayPointersVboMethod;

    public static OptifineCompat getInstance() {
        return ourInstance;
    }

    private boolean enableOptifine = false;

    private OptifineCompat() {
    }

    public void intialize()
    {
        try {
            Class.forName("Config");
            Class.forName("net.optifine.shaders.ShadersRender");
            Structurize.getLogger().info("Optifine found. Enabling compat.");

            setupReflectedMethodReferences();

            enableOptifine = true;
        } catch (ClassNotFoundException e) {
            Structurize.getLogger().info("Optifine not found. Disabling compat.", e);
            enableOptifine = false;
        } catch (NoSuchMethodException e) {
            Structurize.getLogger().error("Optifine found. But could not access related methods.", e);
            enableOptifine = false;
        }
    }

    private void setupReflectedMethodReferences() throws ClassNotFoundException, NoSuchMethodException {
        final Class<?> configClass = Class.forName("Config");
        final Class<?> shaderRenderClass = Class.forName("net.optifine.shaders.ShadersRender");

        isShadersEnabledMethod = configClass.getMethod("isShaders");
        isShadersEnabledMethod.setAccessible(true);

        preRenderChunkLayerMethod = shaderRenderClass.getMethod("preRenderChunkLayer", BlockRenderLayer.class);
        preRenderChunkLayerMethod.setAccessible(true);

        postRenderChunkLayerMethod = shaderRenderClass.getMethod("postRenderChunkLayer", BlockRenderLayer.class);
        postRenderChunkLayerMethod.setAccessible(true);

        setupArrayPointersVboMethod = shaderRenderClass.getMethod("setupArrayPointersVbo");
        setupArrayPointersVboMethod.setAccessible(true);
    }

    public void preTemplateDraw()
    {
        if (!enableOptifine)
            return;

        try {
            if((Boolean) isShadersEnabledMethod.invoke(null))
            {
                preRenderChunkLayerMethod.invoke(null, BlockRenderLayer.SOLID);
            }
        } catch (IllegalAccessException e) {
            Structurize.getLogger().error("Failed to access Optifine related rendering methods.", e);
            Structurize.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        } catch (InvocationTargetException e) {
            Structurize.getLogger().error("Failed to invoke Optifine related rendering methods.", e);
            Structurize.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        }
    }

    public void postTemplateDraw()
    {
        if (!enableOptifine)
            return;

        try {
            if((Boolean) isShadersEnabledMethod.invoke(null))
            {
                postRenderChunkLayerMethod.invoke(null, BlockRenderLayer.SOLID);
            }
        } catch (IllegalAccessException e) {
            Structurize.getLogger().error("Failed to access Optifine related rendering methods.", e);
            Structurize.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        } catch (InvocationTargetException e) {
            Structurize.getLogger().error("Failed to invoke Optifine related rendering methods.", e);
            Structurize.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        }
    }

    public boolean setupArrayPointers()
    {
        if (!enableOptifine)
            return false;

        try {
            if((Boolean) isShadersEnabledMethod.invoke(null))
            {
                setupArrayPointersVboMethod.invoke(null);
            }

            return true;
        } catch (IllegalAccessException e) {
            Structurize.getLogger().error("Failed to access Optifine related rendering methods.", e);
            Structurize.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        } catch (InvocationTargetException e) {
            Structurize.getLogger().error("Failed to invoke Optifine related rendering methods.", e);
            Structurize.getLogger().error("Disabling Optifine Compat.");
            enableOptifine = false;
        }

        return false;
    }

}
