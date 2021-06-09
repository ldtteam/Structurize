package com.ldtteam.structurize.api.util.constant;

import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLCapabilities;

public class OpenGlHelper
{
    public static int lightmapTexUnit = 33985;
    public static int defaultTexUnit =  33984;
    private static boolean arbMultitexture;
    private static boolean init = false;


    public static void setClientActiveTexture(int texture) {

        if (!init)
        {
            initializeTextures();
        }

        if (arbMultitexture) {
            ARBMultitexture.glClientActiveTextureARB(texture);
        } else {
            GL13.glClientActiveTexture(texture);
        }
    }

    public static void initializeTextures()
    {
        GLCapabilities contextcapabilities = GL.getCapabilities();
        arbMultitexture = contextcapabilities.GL_ARB_multitexture && !contextcapabilities.OpenGL13;
    }
}
