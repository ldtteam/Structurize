package com.ldtteam.structurize.util;

import com.ldtteam.structurize.storage.rendering.types.BoxPreviewData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.ldtteam.structurize.api.constants.Constants.MOD_ID;

/**
 * Data representing a set of scans in the scan tool.
 * This is deliberately lazy and accesses only the parts of the tag you actually ask for.
 */
public class ScanToolData
{
    /**
     * The number of scan slots.  We keep 10 so that we can map them to 0-9 keys.
     */
    public static final int NUM_SLOTS = 10;

    private static final String NBT_SLOTS = MOD_ID + ":slots";
    private static final String NBT_CURRENT = MOD_ID + ":cur";

    private final CompoundTag tag;

    /**
     * Load from a tag
     * @param tag the tag
     */
    public ScanToolData(@NotNull CompoundTag tag)
    {
        this.tag = tag;
    }

    /**
     * Gets the internal tag used to store data.  Don't fiddle with this.
     * @return the tag
     */
    @NotNull
    public CompoundTag getInternalTag()
    {
        return this.tag;
    }

    /**
     * Gets the currently selected slot number
     * @return the slot number
     */
    public int getCurrentSlotId()
    {
        // the default slot is #1 so that we can treat slot 0 as if it were slot 10 (but we still call it slot 0)
        return this.tag.contains(NBT_CURRENT) ?  Math.max(0, Math.min(NUM_SLOTS - 1, this.tag.getInt(NBT_CURRENT))) : 1;
    }

    /**
     * Gets the currently selected slot
     * @return the slot data for the current slot
     */
    @NotNull
    public Slot getCurrentSlotData()
    {
        final int current = getCurrentSlotId();
        final ListTag slots = tag.getList(NBT_SLOTS, Tag.TAG_COMPOUND);
        return new Slot(Objects.requireNonNullElse(current < slots.size() ? slots.getCompound(current) : null,new CompoundTag()));
    }

    /**
     * Saves the specified data in the current slot
     * @param data the new slot data
     */
    public void setCurrentSlotData(@Nullable final Slot data)
    {
        final int current = getCurrentSlotId();
        final ListTag slots = tag.getList(NBT_SLOTS, Tag.TAG_COMPOUND);
        while (current >= slots.size()) slots.add(new CompoundTag());
        slots.set(current, data == null ? new CompoundTag() : data.write(new CompoundTag()));
        tag.put(NBT_SLOTS, slots);
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
        this.tag.putInt(NBT_CURRENT, slot);
    }


    /**
     * Data for one scan slot
     */
    public static class Slot
    {
        private final String name;
        private final BoxPreviewData box;

        /**
         * Construct directly
         * @param name the schematic name
         * @param box the schematic box
         */
        public Slot(@NotNull final String name,
                    @NotNull final BoxPreviewData box)
        {
            this.name = name;
            this.box = box;
        }

        /**
         * Load from tag
         * @param tag the tag
         */
        public Slot(@NotNull final CompoundTag tag)
        {
            final BlockPos corner1 = NbtUtils.readBlockPos(tag.getCompound("c1"));
            final BlockPos corner2 = NbtUtils.readBlockPos(tag.getCompound("c2"));
            final Optional<BlockPos> anchor = tag.contains("a")
                    ? Optional.of(NbtUtils.readBlockPos(tag.getCompound("a")))
                    : Optional.empty();
            this.box = new BoxPreviewData(corner1, corner2, anchor);

            this.name = tag.getString("n");
        }

        /**
         * Serialize
         * @param tag target tag
         * @return the same tag (for convenience)
         */
        public CompoundTag write(@NotNull final CompoundTag tag)
        {
            tag.put("c1", NbtUtils.writeBlockPos(this.box.getPos1()));
            tag.put("c2", NbtUtils.writeBlockPos(this.box.getPos2()));
            if (this.box.getAnchor().isPresent())
            {
                tag.put("a", NbtUtils.writeBlockPos(this.box.getAnchor().get()));
            }
            else
            {
                tag.remove("a");
            }
            tag.putString("n", this.name);
            return tag;
        }

        public boolean isEmpty()
        {
            return this.name.isEmpty();
        }

        @NotNull
        public BoxPreviewData getBox()
        {
            return this.box;
        }

        @NotNull
        public String getName()
        {
            return this.name;
        }
    }
}
