package com.ldtteam.structurize.util;

import com.ldtteam.common.codec.Codecs;
import com.ldtteam.structurize.component.ModDataComponents;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

/**
 * Data representing a set of scans in the scan tool.
 */
public class ScanToolData
{
    /**
     * The number of scan slots.  We keep 10 so that we can map them to 0-9 keys.
     */
    public static final int NUM_SLOTS = 10;

    public static final Codec<ScanToolData> CODEC = RecordCodecBuilder.create(builder -> builder
        .group(Codecs.forArray(Slot.CODEC, Slot[]::new, NUM_SLOTS).fieldOf("slots").forGetter(data -> data.slots),
            Codec.intRange(0, NUM_SLOTS - 1).fieldOf("current_slot").forGetter(ScanToolData::getCurrentSlotId),
            Codecs.wrapNullableField(BlockPos.CODEC, "anchor_pos", data -> data.anchorPos),
            Codecs.wrapNullableField(BlockPos.CODEC, "commands_pos", data -> data.commandPos),
            Codec.STRING.fieldOf("structure_name").forGetter(data -> data.structureName),
            Codecs.wrapNullableField(ResourceLocation.CODEC, "dimension_key", data -> data.dimensionKey))
        .apply(builder, (a, b, c, d, e, f) -> new ScanToolData(a, b, c.orElse(null), d.orElse(null), e, f.orElse(null))));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanToolData> STREAM_CODEC =
        StreamCodec.composite(Codecs.streamForArray(Slot.STREAM_CODEC, Slot[]::new, NUM_SLOTS),
            data -> data.slots,
            ByteBufCodecs.VAR_INT,
            ScanToolData::getCurrentSlotId,
            Codecs.wrapNullable(BlockPos.STREAM_CODEC),
            data -> data.anchorPos,
            Codecs.wrapNullable(BlockPos.STREAM_CODEC),
            data -> data.commandPos,
            Codecs.wrapNullable(ByteBufCodecs.STRING_UTF8),
            data -> data.structureName,
            Codecs.wrapNullable(ResourceLocation.STREAM_CODEC),
            data -> data.dimensionKey,
            ScanToolData::new);

    public static DeferredHolder<DataComponentType<?>, DataComponentType<ScanToolData>> TYPE;

    private final Slot[] slots;
    private int currentSlot = 1;

    // public mutable fields

    @Nullable
    public BlockPos anchorPos;
    @Nullable
    public BlockPos commandPos;
    public String structureName = "";
    @Nullable
    public ResourceLocation dimensionKey;

    private ScanToolData(final Slot[] slots,
        final int currentSlot,
        final BlockPos anchorPos,
        final BlockPos commandPos,
        final String structureName,
        final ResourceLocation dimensionKey)
    {
        this.slots = validateSlots(slots);
        this.currentSlot = currentSlot;
        this.anchorPos = anchorPos;
        this.commandPos = commandPos;
        this.structureName = structureName;
        this.dimensionKey = dimensionKey;
    }

    private ScanToolData()
    {
        this.slots = validateSlots(new Slot[NUM_SLOTS]);
    }

    /**
     * @return array with proper length and non-null values
     */
    private Slot[] validateSlots(Slot[] slots)
    {
        if (slots.length != NUM_SLOTS)
        {
            slots = Arrays.copyOf(slots, NUM_SLOTS);
        }

        for (int i = 0; i < NUM_SLOTS; i++)
        {
            if (slots[i] == null)
            {
                slots[i] = Slot.EMPTY;
            }
        }
        return slots;
    }

    /**
     * Gets the currently selected slot number
     * @return the slot number
     */
    public int getCurrentSlotId()
    {
        // the default slot is #1 so that we can treat slot 0 as if it were slot 10 (but we still call it slot 0)
        return currentSlot;
    }

    /**
     * Gets the currently selected slot
     * @return the slot data for the current slot
     */
    @Nullable
    public Slot getCurrentSlotData()
    {
        return slots[currentSlot];
    }

    /**
     * Saves the specified data in the current slot
     * @param data the new slot data
     */
    public void setCurrentSlotData(@Nullable final Slot data)
    {
        slots[currentSlot] = data == null ? Slot.EMPTY : data;
    }

    /**
     * Moves to the next slot, wrapping back to the start if needed
     */
    public void nextSlot()
    {
        moveTo((getCurrentSlotId() + 1) % NUM_SLOTS);
    }

    /**
     * Moves to the previous slot, wrapping to the end if needed
     */
    public void prevSlot()
    {
        moveTo((getCurrentSlotId() + NUM_SLOTS - 1) % NUM_SLOTS);
    }

    /**
     * Moves to the specified slot number
     * @param slot the new slot number
     */
    public void moveTo(final int slot)
    {
        currentSlot = slot;
    }

    public boolean hasAnchorPos()
    {
        return anchorPos != null;
    }

    public boolean hasCommandPos()
    {
        return commandPos != null;
    }

    public boolean hasStructureName()
    {
        return structureName != null && !structureName.isEmpty();
    }

    public boolean hasDimensionKey()
    {
        return dimensionKey != null;
    }

    public static ScanToolData getOrCreate(final ItemStack itemStack)
    {
        ScanToolData data = itemStack.get(ModDataComponents.SCAN_TOOL);
        if (data == null)
        {
            data = new ScanToolData();
            itemStack.set(ModDataComponents.SCAN_TOOL, data);
        }
        return data;
    }

    @Override
    public boolean equals(final Object obj)
    {
        // because neo
        return this == obj;
    }

    @Override
    public int hashCode()
    {
        // because neo
        return super.hashCode();
    }

    /**
     * Data for one scan slot
     */
    public record Slot(@NotNull String getName, @NotNull BoxPreviewData getBox)
    {
        // TODO: reevaluate emptiness and nullness of slots (so we dont need to use BlockPos.ZERO and empty instances)
        // TODO: reevaluate all network messages regarding scan tool (so it matches how data comps are synced)
        public static final Slot EMPTY = new Slot("", new BoxPreviewData(BlockPos.ZERO, BlockPos.ZERO, Optional.empty()));
        
        public static final Codec<Slot> CODEC = Codecs
            .withEmpty(RecordCodecBuilder.create(builder -> builder
                    .group(Codec.STRING.fieldOf("name").forGetter(Slot::getName),
                        BoxPreviewData.CODEC.fieldOf("box").forGetter(Slot::getBox))
                    .apply(builder, Slot::new)),
                EMPTY);
        public static final StreamCodec<RegistryFriendlyByteBuf, Slot> STREAM_CODEC = Codecs.streamWithEmpty(
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, Slot::getName, BoxPreviewData.STREAM_CODEC, Slot::getBox, Slot::new),
            EMPTY);

        public boolean isEmpty()
        {
            return this.getName.isEmpty();
        }
    }
}
