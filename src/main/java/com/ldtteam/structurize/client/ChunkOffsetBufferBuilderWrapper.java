package com.ldtteam.structurize.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Delegating offseted bufferBuilder, delegated method @Overriden in BufferBuilder class to provide fast delegation
 */
public class ChunkOffsetBufferBuilderWrapper implements VertexConsumer
{
    private BufferBuilder delegate;
    private int offsetX;
    private int offsetY;
    private int offsetZ;

    public void setOffset(final BufferBuilder delegate, final int offsetX, final int offsetY, final int offsetZ)
    {
        this.delegate = delegate;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z)
    {
        return delegate.addVertex(offsetX + x, offsetY + y, offsetZ + z);
    }

    @Override
    public VertexConsumer setColor(int p_350581_, int p_350952_, int p_350275_, int p_350985_)
    {
        return delegate.setColor(p_350581_, p_350952_, p_350275_, p_350985_);
    }

    @Override
    public VertexConsumer setNormal(float p_351000_, float p_350982_, float p_350974_)
    {
        return delegate.setNormal(p_351000_, p_350982_, p_350974_);
    }

    @Override
    public VertexConsumer setUv(float p_350574_, float p_350773_)
    {
        return delegate.setUv(p_350574_, p_350773_);
    }

    @Override
    public VertexConsumer setUv1(int p_350396_, int p_350722_)
    {
        return delegate.setUv1(p_350396_, p_350722_);
    }

    @Override
    public VertexConsumer setUv2(int p_351058_, int p_350320_)
    {
        return delegate.setUv2(p_351058_, p_350320_);
    }

    @Override
    public VertexConsumer setColor(int p_350530_)
    {
        return delegate.setColor(p_350530_);
    }

    @Override
    public VertexConsumer setOverlay(int p_350297_)
    {
        return delegate.setOverlay(p_350297_);
    }

    @Override
    public VertexConsumer setLight(int p_350848_)
    {
        return delegate.setLight(p_350848_);
    }

    @Override
    public void addVertex(float x,
        float y,
        float z,
        int p_350371_,
        float p_350977_,
        float p_350674_,
        int p_350816_,
        int p_350690_,
        float p_350640_,
        float p_350490_,
        float p_350810_)
    {
        delegate.addVertex(offsetX + x, offsetY + y, offsetZ + z, p_350371_, p_350977_, p_350674_, p_350816_, p_350690_, p_350640_, p_350490_, p_350810_);
    }
}
