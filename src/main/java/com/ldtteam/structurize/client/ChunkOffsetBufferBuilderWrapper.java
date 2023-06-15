package com.ldtteam.structurize.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.nio.ByteBuffer;

/**
 * INLINE: delegate class, check method overrides
 */
public class ChunkOffsetBufferBuilderWrapper extends BufferBuilder
{
    private static final ChunkOffsetBufferBuilderWrapper INSTANCE = new ChunkOffsetBufferBuilderWrapper();

    private ChunkOffsetBufferBuilderWrapper()
    {
        super(0);
    }

    private BufferBuilder delegate;
    private int offsetX;
    private int offsetY;
    private int offsetZ;

    public static ChunkOffsetBufferBuilderWrapper setupGlobalInstance(final BufferBuilder delegate,
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
    public void defaultColor(int p_85830_, int p_85831_, int p_85832_, int p_85833_)
    {
        delegate.defaultColor(p_85830_, p_85831_, p_85832_, p_85833_);
    }

    @Override
    public VertexConsumer vertex(double p_85945_, double p_85946_, double p_85947_)
    {
        return delegate.vertex(offsetX + p_85945_, offsetY + p_85946_, offsetZ + p_85947_);
    }

    @Override
    public void unsetDefaultColor()
    {
        delegate.unsetDefaultColor();
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData)
    {
        return delegate.misc(element, rawData);
    }

    @Override
    public VertexConsumer uv(float p_85948_, float p_85949_)
    {
        return delegate.uv(p_85948_, p_85949_);
    }

    @Override
    public VertexConsumer overlayCoords(int p_85971_, int p_85972_)
    {
        return delegate.overlayCoords(p_85971_, p_85972_);
    }

    @Override
    public VertexConsumer uv2(int p_86010_, int p_86011_)
    {
        return delegate.uv2(p_86010_, p_86011_);
    }

    @Override
    public VertexConsumer normal(float p_86005_, float p_86006_, float p_86007_)
    {
        return delegate.normal(p_86005_, p_86006_, p_86007_);
    }

    @Override
    public void putBulkData(Pose pose,
        BakedQuad bakedQuad,
        float red,
        float green,
        float blue,
        float alpha,
        int packedLight,
        int packedOverlay,
        boolean readExistingColor)
    {
        delegate.putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay, readExistingColor);
    }

    @Override
    public int applyBakedLighting(int packedLight, ByteBuffer data)
    {
        return delegate.applyBakedLighting(packedLight, data);
    }

    @Override
    public VertexConsumer color(float p_85951_, float p_85952_, float p_85953_, float p_85954_)
    {
        return delegate.color(p_85951_, p_85952_, p_85953_, p_85954_);
    }

    @Override
    public VertexConsumer color(int p_193480_)
    {
        return delegate.color(p_193480_);
    }

    @Override
    public void applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform)
    {
        delegate.applyBakedNormals(generated, data, normalTransform);
    }

    @Override
    public VertexConsumer uv2(int p_85970_)
    {
        return delegate.uv2(p_85970_);
    }

    @Override
    public VertexConsumer overlayCoords(int p_86009_)
    {
        return delegate.overlayCoords(p_86009_);
    }

    @Override
    public void setQuadSorting(VertexSorting p_277454_)
    {
        delegate.setQuadSorting(p_277454_);
    }

    @Override
    public void putBulkData(Pose p_85988_,
        BakedQuad p_85989_,
        float p_85990_,
        float p_85991_,
        float p_85992_,
        int p_85993_,
        int p_85994_)
    {
        delegate.putBulkData(p_85988_, p_85989_, p_85990_, p_85991_, p_85992_, p_85993_, p_85994_);
    }

    @Override
    public int hashCode()
    {
        return delegate.hashCode();
    }

    @Override
    public SortState getSortState()
    {
        return delegate.getSortState();
    }

    @Override
    public VertexConsumer uvShort(short p_85794_, short p_85795_, int p_85796_)
    {
        return delegate.uvShort(p_85794_, p_85795_, p_85796_);
    }

    @Override
    public void putBulkData(Pose p_85996_,
        BakedQuad p_85997_,
        float[] p_85998_,
        float p_85999_,
        float p_86000_,
        float p_86001_,
        int[] p_86002_,
        int p_86003_,
        boolean p_86004_)
    {
        delegate.putBulkData(p_85996_, p_85997_, p_85998_, p_85999_, p_86000_, p_86001_, p_86002_, p_86003_, p_86004_);
    }

    @Override
    public void restoreSortState(SortState p_166776_)
    {
        delegate.restoreSortState(p_166776_);
    }

    @Override
    public void putBulkData(Pose p_85996_,
        BakedQuad p_85997_,
        float[] p_85998_,
        float p_85999_,
        float p_86000_,
        float p_86001_,
        float alpha,
        int[] p_86002_,
        int p_86003_,
        boolean p_86004_)
    {
        delegate.putBulkData(p_85996_, p_85997_, p_85998_, p_85999_, p_86000_, p_86001_, alpha, p_86002_, p_86003_, p_86004_);
    }

    @Override
    public void begin(Mode p_166780_, VertexFormat p_166781_)
    {
        delegate.begin(p_166780_, p_166781_);
    }

    @Override
    public boolean equals(Object obj)
    {
        return delegate.equals(obj);
    }

    @Override
    public VertexConsumer vertex(Matrix4f p_254075_, float p_254519_, float p_253869_, float p_253980_)
    {
        return delegate.vertex(p_254075_, p_254519_, p_253869_, p_253980_);
    }

    @Override
    public VertexConsumer normal(Matrix3f p_253747_, float p_254430_, float p_253877_, float p_254167_)
    {
        return delegate.normal(p_253747_, p_254430_, p_253877_, p_254167_);
    }

    @Override
    public boolean isCurrentBatchEmpty()
    {
        return delegate.isCurrentBatchEmpty();
    }

    @Override
    public RenderedBuffer endOrDiscardIfEmpty()
    {
        return delegate.endOrDiscardIfEmpty();
    }

    @Override
    public RenderedBuffer end()
    {
        return delegate.end();
    }

    @Override
    public void putByte(int p_85686_, byte p_85687_)
    {
        delegate.putByte(p_85686_, p_85687_);
    }

    @Override
    public void putShort(int p_85700_, short p_85701_)
    {
        delegate.putShort(p_85700_, p_85701_);
    }

    @Override
    public void putFloat(int p_85689_, float p_85690_)
    {
        delegate.putFloat(p_85689_, p_85690_);
    }

    @Override
    public void endVertex()
    {
        delegate.endVertex();
    }

    @Override
    public void nextElement()
    {
        delegate.nextElement();
    }

    @Override
    public VertexConsumer color(int p_85692_, int p_85693_, int p_85694_, int p_85695_)
    {
        return delegate.color(p_85692_, p_85693_, p_85694_, p_85695_);
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }

    @Override
    public void vertex(float p_85671_,
        float p_85672_,
        float p_85673_,
        float p_85674_,
        float p_85675_,
        float p_85676_,
        float p_85677_,
        float p_85678_,
        float p_85679_,
        int p_85680_,
        int p_85681_,
        float p_85682_,
        float p_85683_,
        float p_85684_)
    {
        delegate.vertex(p_85671_,
            p_85672_,
            p_85673_,
            p_85674_,
            p_85675_,
            p_85676_,
            p_85677_,
            p_85678_,
            p_85679_,
            p_85680_,
            p_85681_,
            p_85682_,
            p_85683_,
            p_85684_);
    }

    @Override
    public void clear()
    {
        delegate.clear();
    }

    @Override
    public void discard()
    {
        delegate.discard();
    }

    @Override
    public VertexFormatElement currentElement()
    {
        return delegate.currentElement();
    }

    @Override
    public boolean building()
    {
        return delegate.building();
    }

    @Override
    public void putBulkData(ByteBuffer buffer)
    {
        delegate.putBulkData(buffer);
    }
}
