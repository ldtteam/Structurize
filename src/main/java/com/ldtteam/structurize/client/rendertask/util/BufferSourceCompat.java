package com.ldtteam.structurize.client.rendertask.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.HashMap;
import java.util.Map;

/**
 * Compatibility shim replacing the removed MultiBufferSource.BufferSource.
 * Manages per-RenderType BufferBuilder instances for immediate-mode rendering.
 */
public final class BufferSourceCompat
{
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final Map<RenderType, BufferBuilder> builders = new HashMap<>();
    private final ByteBufferBuilder fallbackBuffer = new ByteBufferBuilder(DEFAULT_BUFFER_SIZE);

    public BufferSourceCompat()
    {
    }

    public VertexConsumer getBuffer(final RenderType renderType)
    {
        return builders.computeIfAbsent(renderType, type -> new BufferBuilder(
            new ByteBufferBuilder(RenderType.BIG_BUFFER_SIZE),
            type.primitiveTopology(),
            type.format()));
    }

    public void endBatch()
    {
        builders.clear();
    }
}
