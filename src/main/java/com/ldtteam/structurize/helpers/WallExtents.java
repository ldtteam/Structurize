package com.ldtteam.structurize.helpers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * A description/config for wall replication.
 */
public final class WallExtents
{
    private short negative;
    private short positive;
    private short overlap;

    /** Default construction */
    public WallExtents()
    {
        this((short) 0, (short) 0, (short) 0);
    }

    /** Explicit construction */
    public WallExtents(final short negative, final short positive, final short overlap)
    {
        this.negative = negative;
        this.positive = positive;
        this.overlap = overlap;
    }

    /** Copy constructor */
    public WallExtents(@NotNull final WallExtents extents)
    {
        this(extents.negative, extents.positive, extents.overlap);
    }

    /** Get wall extent in the negative direction */
    public short getNegative()
    {
        return this.negative;
    }

    /** Get wall extent in the positive direction */
    public short getPositive()
    {
        return this.positive;
    }

    /** Get wall overlap (number of blocks) */
    public short getOverlap()
    {
        return this.overlap;
    }

    /** Set extents */
    public WallExtents set(final short negative, final short positive, final short overlap)
    {
        this.negative = negative;
        this.positive = positive;
        this.overlap = overlap;
        return this;
    }

    /** Extend by one copy in the negative direction */
    public WallExtents extendNegative()
    {
        ++this.negative;
        return this;
    }

    /** Reduce by one copy in the negative direction */
    public WallExtents reduceNegative()
    {
        this.negative = (short) Math.max(0, this.negative - 1);
        return this;
    }

    /** Extend by one copy in the positive direction */
    public WallExtents extendPositive()
    {
        ++this.positive;
        return this;
    }

    /** Reduce by one copy in the positive direction */
    public WallExtents reducePositive()
    {
        this.positive = (short) Math.max(0, this.positive - 1);
        return this;
    }

    /** Extend overlap by one block (reduce total length) */
    public WallExtents extendOverlap()
    {
        ++this.overlap;
        return this;
    }

    /** Reduce overlap by one block (increase total length) */
    public WallExtents reduceOverlap()
    {
        this.overlap = (short) Math.max(0, this.overlap - 1);
        return this;
    }

    /** True if wall mode is enabled */
    public boolean isEnabled()
    {
        return this.negative > 0 || this.positive > 0;
    }

    /** Get the total number of copies (including the original) */
    public short getTotalCopies()
    {
        return (short) (1 + this.negative + this.positive);
    }

    /** Serialize to network */
    public void write(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeVarInt(this.negative);
        buf.writeVarInt(this.positive);
        buf.writeVarInt(this.overlap);
    }

    /** Deserialize from network */
    public static WallExtents read(@NotNull final FriendlyByteBuf buf)
    {
        final short negative = (short) buf.readVarInt();
        final short positive = (short) buf.readVarInt();
        final short overlap = (short) buf.readVarInt();

        return new WallExtents(negative, positive, overlap);
    }

    /** Serialiase to NBT */
    public void write(@NotNull final CompoundTag compound)
    {
        compound.putInt("wallneg", this.negative);
        compound.putInt("wallpos", this.positive);
        compound.putInt("wallovl", this.overlap);
    }

    /** Deserialize from NBT */
    public static WallExtents read(@NotNull final CompoundTag compound)
    {
        final short negative = compound.getShort("wallneg");
        final short positive = compound.getShort("wallpos");
        final short overlap = compound.getShort("wallovl");
        
        return new WallExtents(negative, positive, overlap);
    }
}
