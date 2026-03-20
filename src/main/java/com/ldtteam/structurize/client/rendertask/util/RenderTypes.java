package com.ldtteam.structurize.client.rendertask.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

public class RenderTypes extends RenderType
{
    private RenderTypes(
        final String nameIn,
        final VertexFormat formatIn,
        final VertexFormat.Mode drawModeIn,
        final int bufferSizeIn,
        final boolean useDelegateIn,
        final boolean needsSortingIn,
        final Runnable setupTaskIn,
        final Runnable clearTaskIn)
    {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        throw new IllegalStateException();
    }

    /**
     * Usable for rendering simple flat textures
     *
     * @param resLoc texture location
     * @return render type
     */
    public static RenderType worldEntityIcon(final ResourceLocation resLoc)
    {
        return RenderTypes.WORLD_ENTITY_ICON.apply(resLoc);
    }

    private static final DepthTestStateShard ALWAYS_DEPTH_TEST  = new AlwaysDepthTestStateShard();
    private static final DepthTestStateShard GREATER_DEPTH_TEST = new DepthTestStateShard(">", GL11.GL_GREATER);

    private static final Function<ResourceLocation, RenderType> WORLD_ENTITY_ICON = Util.memoize((p_173202_) -> {
        return create("structurize:entity_icon",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1024,
            false,
            true,
            CompositeState.builder()
                .setShaderState(POSITION_TEX_SHADER)
                .setTextureState(new TextureStateShard(p_173202_, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(ALWAYS_DEPTH_TEST)
                .createCompositeState(false));
    });

    /**
     * Used to draw overlay lines that only appear outside existing blocks.
     */
    public static final RenderType LINES_OUTSIDE_BLOCKS = create("structurize:lines_outside_blocks",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1024,
        false,
        false,
        CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    /**
     * Used to draw overlay lines that only appear inside existing blocks.
     */
    public static final RenderType LINES_INSIDE_BLOCKS = create("structurize:lines_inside_blocks",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1024,
        false,
        false,
        CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(GREATER_DEPTH_TEST)
            .setCullState(CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    public static final RenderType GLINT_LINES = create("structurize_glint_lines",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.DEBUG_LINES,
        1 << 12,
        false,
        false,
        RenderType.CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setDepthTestState(NO_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    public static final RenderType GLINT_LINES_WITH_WIDTH = create("structurize_glint_lines_with_width",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1 << 13,
        false,
        false,
        RenderType.CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setDepthTestState(AlwaysDepthTestStateShard.ALWAYS_DEPTH_TEST)
            .setCullState(CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .createCompositeState(false));

    public static final RenderType LINES = create("structurize_lines",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.DEBUG_LINES,
        1 << 14,
        false,
        false,
        RenderType.CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    public static final RenderType LINES_WITH_WIDTH = create("structurize_lines_with_width",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1 << 13,
        false,
        false,
        RenderType.CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .createCompositeState(false));

    public static final RenderType COLORED_TRIANGLES = create("structurize_colored_triangles",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1 << 13,
        false,
        false,
        RenderType.CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setCullState(CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .createCompositeState(false));

    public static final RenderType COLORED_TRIANGLES_NC_ND = create("structurize_colored_triangles_nc_nd",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.TRIANGLES,
        1 << 12,
        false,
        false,
        RenderType.CompositeState.builder()
            .setTextureState(NO_TEXTURE)
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDepthTestState(NO_DEPTH_TEST)
            .setCullState(NO_CULL)
            .setLightmapState(NO_LIGHTMAP)
            .setOverlayState(NO_OVERLAY)
            .setLayeringState(NO_LAYERING)
            .setOutputState(MAIN_TARGET)
            .setTexturingState(DEFAULT_TEXTURING)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    private static class AlwaysDepthTestStateShard extends DepthTestStateShard
    {
        public static final DepthTestStateShard ALWAYS_DEPTH_TEST = new AlwaysDepthTestStateShard();

        private AlwaysDepthTestStateShard()
        {
            super("true_always", -1);
            setupState = () -> {
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_ALWAYS);
            };
        }
    }
}
