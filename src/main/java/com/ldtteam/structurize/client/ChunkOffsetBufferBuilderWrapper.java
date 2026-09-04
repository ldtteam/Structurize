package com.ldtteam.structurize.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Routes chunk-local vertices into the blueprint's world-space buffer.
 */
public final class ChunkOffsetBufferBuilderWrapper implements VertexConsumer
{
    private static final ChunkOffsetBufferBuilderWrapper INSTANCE = new ChunkOffsetBufferBuilderWrapper();

    private VertexConsumer delegate;
    private int offsetX;
    private int offsetY;
    private int offsetZ;

    private ChunkOffsetBufferBuilderWrapper()
    {
        this.delegate = VertexConsumer.class.cast(null);
    }

    public static ChunkOffsetBufferBuilderWrapper setupGlobalInstance(
        final VertexConsumer delegate,
        final int offsetX,
        final int offsetY,
        final int offsetZ)
    {
        INSTANCE.delegate = delegate;
        INSTANCE.offsetX = offsetX;
        INSTANCE.offsetY = offsetY;
        INSTANCE.offsetZ = offsetZ;
        return INSTANCE;
    }

    @Override
    public VertexConsumer addVertex(final float x, final float y, final float z)
    {
        delegate.addVertex(offsetX + x, offsetY + y, offsetZ + z);
        return this;
    }

    @Override
    public VertexConsumer addVertex(final Matrix4fc pose, final float x, final float y, final float z)
    {
        final Vector3f position = pose.transformPosition(offsetX + x, offsetY + y, offsetZ + z, new Vector3f());
        delegate.addVertex(position.x(), position.y(), position.z());
        return this;
    }

    @Override
    public VertexConsumer addVertex(final Vector3fc position)
    {
        return addVertex(position.x(), position.y(), position.z());
    }

    @Override
    public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha)
    {
        delegate.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer setColor(final int color)
    {
        delegate.setColor(color);
        return this;
    }

    @Override
    public VertexConsumer setUv(final float u, final float v)
    {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(final int u, final int v)
    {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(final int u, final int v)
    {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(final float x, final float y, final float z)
    {
        delegate.setNormal(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(final float width)
    {
        delegate.setLineWidth(width);
        return this;
    }
}
