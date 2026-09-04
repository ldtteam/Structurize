package com.ldtteam.structurize.client.rendertask.util;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * Structurize's overlay render types, expressed with the current render pipeline API.
 */
public final class RenderTypes
{
    private RenderTypes()
    {
    }

    public static RenderType worldEntityIcon(final Identifier texture)
    {
        return WORLD_ENTITY_ICON.apply(texture);
    }

    public static final RenderType LINES_OUTSIDE_BLOCKS = positionColor(
        "structurize:lines_outside_blocks",
        com.mojang.blaze3d.PrimitiveTopology.TRIANGLES,
        BlendFunction.TRANSLUCENT,
        CompareOp.LESS_THAN_OR_EQUAL,
        false,
        true,
        1024);

    public static final RenderType LINES_INSIDE_BLOCKS = positionColor(
        "structurize:lines_inside_blocks",
        com.mojang.blaze3d.PrimitiveTopology.TRIANGLES,
        BlendFunction.TRANSLUCENT,
        CompareOp.GREATER_THAN,
        false,
        true,
        1024);

    public static final RenderType GLINT_LINES = positionColor(
        "structurize_glint_lines",
        com.mojang.blaze3d.PrimitiveTopology.DEBUG_LINES,
        BlendFunction.ADDITIVE,
        CompareOp.ALWAYS_PASS,
        false,
        false,
        1 << 12);

    public static final RenderType GLINT_LINES_WITH_WIDTH = positionColor(
        "structurize_glint_lines_with_width",
        com.mojang.blaze3d.PrimitiveTopology.TRIANGLES,
        BlendFunction.ADDITIVE,
        CompareOp.ALWAYS_PASS,
        true,
        true,
        1 << 13);

    public static final RenderType LINES = positionColor(
        "structurize_lines",
        com.mojang.blaze3d.PrimitiveTopology.DEBUG_LINES,
        BlendFunction.TRANSLUCENT,
        CompareOp.LESS_THAN_OR_EQUAL,
        false,
        false,
        1 << 14);

    public static final RenderType LINES_WITH_WIDTH = positionColor(
        "structurize_lines_with_width",
        com.mojang.blaze3d.PrimitiveTopology.TRIANGLES,
        BlendFunction.TRANSLUCENT,
        CompareOp.LESS_THAN_OR_EQUAL,
        true,
        true,
        1 << 13);

    public static final RenderType COLORED_TRIANGLES = positionColor(
        "structurize_colored_triangles",
        com.mojang.blaze3d.PrimitiveTopology.TRIANGLES,
        BlendFunction.TRANSLUCENT,
        CompareOp.LESS_THAN_OR_EQUAL,
        true,
        true,
        1 << 13);

    public static final RenderType COLORED_TRIANGLES_NC_ND = positionColor(
        "structurize_colored_triangles_nc_nd",
        com.mojang.blaze3d.PrimitiveTopology.TRIANGLES,
        BlendFunction.TRANSLUCENT,
        CompareOp.ALWAYS_PASS,
        false,
        false,
        1 << 12);

    private static final Function<Identifier, RenderType> WORLD_ENTITY_ICON = Util.memoize(texture -> {
        final RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("structurize", "pipeline/entity_icon"))
            .withVertexShader("core/position_tex")
            .withFragmentShader("core/position_tex")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(com.mojang.blaze3d.PrimitiveTopology.QUADS)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();
        return RenderType.create(
            "structurize:entity_icon",
            RenderSetup.builder(pipeline).withTexture("Sampler0", texture).createRenderSetup());
    });

    private static RenderType positionColor(final String name,
        final com.mojang.blaze3d.PrimitiveTopology mode,
        final BlendFunction blendFunction,
        final CompareOp depthTest,
        final boolean writeDepth,
        final boolean cull,
        final int bufferSize)
    {
        final RenderPipeline pipeline = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("structurize", "pipeline/" + name.substring(name.indexOf(':') + 1)))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(blendFunction))
            .withCull(cull)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(mode)
            .withDepthStencilState(new DepthStencilState(depthTest, writeDepth))
            .build();
        return RenderType.create(name, RenderSetup.builder(pipeline).createRenderSetup());
    }
}
