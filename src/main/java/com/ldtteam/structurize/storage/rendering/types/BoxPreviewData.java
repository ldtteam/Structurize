package com.ldtteam.structurize.storage.rendering.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;
import java.util.Optional;

/**
 * Preview data for box contexts.
 */
public final class BoxPreviewData
{
    public static final Codec<BoxPreviewData> CODEC = RecordCodecBuilder.create(
        builder -> builder
            .group(BlockPos.CODEC.fieldOf("pos1").forGetter(BoxPreviewData::pos1),
                BlockPos.CODEC.fieldOf("pos2").forGetter(BoxPreviewData::pos2),
                BlockPos.CODEC.optionalFieldOf("anchor").forGetter(BoxPreviewData::anchor))
            .apply(builder, BoxPreviewData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoxPreviewData> STREAM_CODEC =
        StreamCodec.composite(BlockPos.STREAM_CODEC,
            BoxPreviewData::pos1,
            BlockPos.STREAM_CODEC,
            BoxPreviewData::pos2,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
            BoxPreviewData::anchor,
            BoxPreviewData::new);
    private final BlockPos           pos1;
    private final BlockPos           pos2;
    private final Optional<BlockPos> anchor;

    /**
     * @param pos1   the first pos.
     * @param pos2   the second pos.
     * @param anchor the anchor of the box.
     */
    public BoxPreviewData(BlockPos pos1, BlockPos pos2, Optional<BlockPos> anchor)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.anchor = anchor;
    }

    private long expireTime = Long.MAX_VALUE;

    /**
     * Update the corners.
     *
     * @param pos1 the new first corner.
     * @param pos2 the new second corner.
     * @return the new box with updated corners.
     */
    public BoxPreviewData withCorners(final BlockPos pos1, final BlockPos pos2)
    {
        return new BoxPreviewData(pos1, pos2, anchor);
    }

    /**
     * Update the anchor position.
     *
     * @param anchor the new anchor position.
     * @return the new box with updated anchor.
     */
    public BoxPreviewData withAnchor(final Optional<BlockPos> anchor)
    {
        return new BoxPreviewData(pos1, pos2, anchor);
    }

    public boolean isExpired()
    {
        return System.currentTimeMillis() - expireTime > 0;
    }

    public void setExpireTime(final int seconds)
    {
        expireTime = System.currentTimeMillis() + seconds * 1000;
    }

    public BlockPos pos1() {return pos1;}

    public BlockPos pos2() {return pos2;}

    public Optional<BlockPos> anchor() {return anchor;}

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }
        var that = (BoxPreviewData) obj;
        return Objects.equals(this.pos1, that.pos1) &&
            Objects.equals(this.pos2, that.pos2) &&
            Objects.equals(this.anchor, that.anchor);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pos1, pos2, anchor);
    }

    @Override
    public String toString()
    {
        return "BoxPreviewData[" +
            "pos1=" + pos1 + ", " +
            "pos2=" + pos2 + ", " +
            "anchor=" + anchor + ']';
    }
}
