package com.ldtteam.structurize.util;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Data representing a set of scans in the scan tool.
 * @param slots the list of slot data.
 * @param currentSlotId the currently selected slot id.
 * @param commandPos the location of the linked command block.
 * @param dimension the dimension of the linked command block.
 */
public record ScanToolData(List<Slot> slots, int currentSlotId,
                           @Nullable BlockPos commandPos, @Nullable ResourceKey<Level> dimension)
{
    /**
     * The number of scan slots.  We keep 10 so that we can map them to 0-9 keys.
     */
    public static final int NUM_SLOTS = 10;

    public static final Codec<ScanToolData> CODEC = RecordCodecBuilder.create(builder -> builder
        .group(Slot.CODEC.listOf().fieldOf("slots").forGetter(data -> data.slots),
            Codec.intRange(0, NUM_SLOTS - 1).fieldOf("current_slot").forGetter(ScanToolData::currentSlotId),
            BlockPos.CODEC.optionalFieldOf("commands_pos").forGetter(data -> Optional.ofNullable(data.commandPos)),
            Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension_key").forGetter(data -> Optional.ofNullable(data.dimension)))
        .apply(builder, ScanToolData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScanToolData> STREAM_CODEC =
        StreamCodec.composite(Slot.STREAM_CODEC.apply(ByteBufCodecs.list()), data -> data.slots,
            ByteBufCodecs.VAR_INT, ScanToolData::currentSlotId,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), data -> Optional.ofNullable(data.commandPos),
            ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), data -> Optional.ofNullable(data.dimension),
            ScanToolData::new);

    public static DeferredHolder<DataComponentType<?>, DataComponentType<ScanToolData>> TYPE;

    private ScanToolData(final List<Slot> slots,
        final int currentSlotId,
        final Optional<BlockPos> commandPos,
        final Optional<ResourceKey<Level>> dimension)
    {
        this(new ArrayList<>(slots), currentSlotId, commandPos.orElse(null), dimension.orElse(null));
    }

    public ScanToolData()
    {
        this(new ArrayList<>(), 1, (BlockPos) null, null);
    }

    public ScanToolData(final List<Slot> slots,
                        final int currentSlotId,
                        @Nullable final BlockPos commandPos,
                        @Nullable final ResourceKey<Level> dimension)
    {
        final List<Slot> newSlots = new ArrayList<>(slots);
        while (newSlots.size() > NUM_SLOTS || (!newSlots.isEmpty() && newSlots.getLast().equals(Slot.EMPTY)))
        {
            newSlots.removeLast();
        }

        this.slots = Collections.unmodifiableList(newSlots);
        this.currentSlotId = currentSlotId;
        this.commandPos = commandPos;
        this.dimension = dimension;
    }

    /**
     * Gets the currently selected slot
     * @return the slot data for the current slot
     */
    public Slot currentSlot()
    {
        return currentSlotId < slots.size() ? slots.get(currentSlotId) : Slot.EMPTY;
    }

    /**
     * Saves the specified data in the current slot.
     * @param data the new slot data.
     * @return the new {@link ScanToolData}.
     */
    public ScanToolData withCurrentSlot(@Nullable final Slot data)
    {
        List<Slot> newSlots = new ArrayList<>(slots);
        while (currentSlotId >= newSlots.size())
        {
            newSlots.add(Slot.EMPTY);
        }
        newSlots.set(currentSlotId, data == null ? Slot.EMPTY : data);

        return new ScanToolData(newSlots, currentSlotId, commandPos, dimension);
    }

    /**
     * Moves to the next slot, wrapping back to the start if needed
     */
    public ScanToolData nextSlot()
    {
        return moveTo((currentSlotId() + 1) % NUM_SLOTS);
    }

    /**
     * Moves to the previous slot, wrapping to the end if needed
     */
    public ScanToolData prevSlot()
    {
        return moveTo((currentSlotId() + NUM_SLOTS - 1) % NUM_SLOTS);
    }

    /**
     * Moves to the specified slot number
     * @param slot the new slot number
     */
    public ScanToolData moveTo(final int slot)
    {
        return new ScanToolData(slots, slot, commandPos, dimension);
    }

    /**
     * Sets the command block position and dimension.
     * @param commandBlock the command block entity.
     * @return the updated {@link ScanToolData}.
     */
    public ScanToolData withCommandBlock(@Nullable final CommandBlockEntity commandBlock)
    {
        return commandBlock == null
                ? new ScanToolData(slots, currentSlotId, (BlockPos) null, null)
                : new ScanToolData(slots, currentSlotId, commandBlock.getBlockPos(), commandBlock.getLevel().dimension());
    }

    /**
     * Gets the {@link ScanToolData} from an {@link ItemStack}.
     * @param stack the stack to query.
     * @return the associated data.
     */
    public static ScanToolData get(final ItemStack stack)
    {
        return stack.getOrDefault(TYPE, new ScanToolData());
    }

    /**
     * Modifies the {@link ScanToolData} on an {@link ItemStack}.
     * @param stack   the stack to update.
     * @param updater the update actions to apply.
     * @return the updated data (also stored on the stack).
     */
    public static ScanToolData update(final ItemStack stack, final UnaryOperator<ScanToolData> updater)
    {
        stack.update(TYPE, new ScanToolData(), updater);
        return stack.get(TYPE);
    }

    /**
     * Data for one scan slot
     */
    public record Slot(@NotNull String name, @NotNull BoxPreviewData box)
    {
        public static final Slot EMPTY = new Slot("", new BoxPreviewData(BlockPos.ZERO, BlockPos.ZERO, Optional.empty()));
        
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(builder -> builder
                    .group(Codec.STRING.fieldOf("name").forGetter(Slot::name),
                        BoxPreviewData.CODEC.fieldOf("box").forGetter(Slot::box))
                    .apply(builder, Slot::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Slot> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, Slot::name,
                        BoxPreviewData.STREAM_CODEC, Slot::box,
                        Slot::new);

        /**
         * Updates the name of the slot.
         * @param name the new name.
         * @return the {@link Slot} with the updated value.
         */
        public Slot withName(final String name)
        {
            return new Slot(name, box);
        }

        /**
         * Updates the box of the slot.
         * @param box the new box.
         * @return the {@link Slot} with the updated value.
         */
        public Slot withBox(final BoxPreviewData box)
        {
            return new Slot(name, box);
        }
    }
}
