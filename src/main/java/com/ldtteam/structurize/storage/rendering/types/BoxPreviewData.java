package com.ldtteam.structurize.storage.rendering.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Preview data for box contexts.
 */
public class BoxPreviewData
{
    public static final Codec<BoxPreviewData> CODEC = RecordCodecBuilder.create(
        builder -> builder
            .group(BlockPos.CODEC.fieldOf("pos1").forGetter(BoxPreviewData::getPos1),
                BlockPos.CODEC.fieldOf("pos2").forGetter(BoxPreviewData::getPos2),
                BlockPos.CODEC.optionalFieldOf("anchor").forGetter(BoxPreviewData::getAnchor))
            .apply(builder, BoxPreviewData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BoxPreviewData> STREAM_CODEC =
        StreamCodec.composite(BlockPos.STREAM_CODEC,
            BoxPreviewData::getPos1,
            BlockPos.STREAM_CODEC,
            BoxPreviewData::getPos2,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
            BoxPreviewData::getAnchor,
            BoxPreviewData::new);

    @NotNull
    private final BlockPos pos1;

    @NotNull
    private final BlockPos pos2;

    @NotNull
    private Optional<BlockPos> anchor;

    /**
     * Create a new box.
     * @param pos1 the first pos.
     * @param pos2 the second pos.
     * @param anchor the anchor of the box.
     */
    public BoxPreviewData(final @NotNull BlockPos pos1, final @NotNull BlockPos pos2, final @NotNull Optional<BlockPos> anchor)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.anchor = anchor;
    }

    public BlockPos getPos1()
    {
        return pos1;
    }

    public BlockPos getPos2()
    {
        return pos2;
    }

    public Optional<BlockPos> getAnchor()
    {
        return anchor;
    }

    public void setAnchor(final Optional<BlockPos> anchor)
    {
        this.anchor = anchor;
    }
}
