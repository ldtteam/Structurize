package com.ldtteam.structurize.storage.rendering.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

/**
 * Preview data for box contexts.
 * @param pos1 the first pos.
 * @param pos2 the second pos.
 * @param anchor the anchor of the box.
 */
public record BoxPreviewData(BlockPos pos1, BlockPos pos2, Optional<BlockPos> anchor)
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

    /**
     * Update the corners.
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
     * @param anchor the new anchor position.
     * @return the new box with updated anchor.
     */
    public BoxPreviewData withAnchor(final Optional<BlockPos> anchor)
    {
        return new BoxPreviewData(pos1, pos2, anchor);
    }
}
