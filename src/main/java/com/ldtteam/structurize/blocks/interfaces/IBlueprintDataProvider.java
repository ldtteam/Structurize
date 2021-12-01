package com.ldtteam.structurize.blocks.interfaces;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;

import java.util.*;
import java.util.function.Function;

/**
 * Interface for TE's which hold schematic specific data. They need to write and read the data to nbt to save it!
 */
public interface IBlueprintDataProvider
{
    public final static String TAG_SCHEMATIC_NAME = "schematicName";
    public final static String TAG_CORNER_ONE     = "corner1";
    public final static String TAG_CORNER_TWO     = "corner2";
    public final static String TAG_TAG_POS        = "tagPos";
    public final static String TAG_TAG_NAME       = "tagName";
    public final static String TAG_TAG_NAME_LIST  = "tagNameList";
    public final static String TAG_POS_TAG_MAP    = "posTagMap";
    public final static String TAG_BLUEPRINTDATA  = "blueprintDataProvider";

    /**
     * Gets the schematic name, required to be saved
     *
     * @return schematic name
     */
    public String getSchematicName();

    /**
     * Sets the schematic name
     *
     * @param name name to set
     */
    public void setSchematicName(final String name);

    /**
     * Gets the map of blockpos and its applied tags. Positions are relative to the anchorpos, use pos + anchor bos to obtain real world coords.
     *
     * @return tagPosMap
     */
    public Map<BlockPos, List<String>> getPositionedTags();

    /**
     * Sets the map of positioned tags.
     *
     * @param positionedTags tag map
     */
    public void setPositionedTags(Map<BlockPos, List<String>> positionedTags);

    /**
     * Gets the schematics corner positions, default is anchor Pos
     */
    public Tuple<BlockPos, BlockPos> getSchematicCorners();

    /**
     * Gets world positions
     */
    default Tuple<BlockPos, BlockPos> getInWorldCorners()
    {
        Tuple<BlockPos, BlockPos> schematicCorners = getSchematicCorners();
        return new Tuple<>(getTilePos().offset(schematicCorners.getA()), getTilePos().offset(getSchematicCorners().getB()));
    }

    /**
     * Sets the schematics corner positions
     *
     * @param pos1 first pos
     * @param pos2 second pos
     */
    public void setSchematicCorners(BlockPos pos1, BlockPos pos2);

    /**
     * Default write to nbt
     */
    default void writeSchematicDataToNBT(final CompoundTag originalCompound)
    {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putString(TAG_SCHEMATIC_NAME, getSchematicName());
        BlockPosUtil.writeToNBT(compoundNBT, TAG_CORNER_ONE, getSchematicCorners().getA());
        BlockPosUtil.writeToNBT(compoundNBT, TAG_CORNER_TWO, getSchematicCorners().getB());

        writeMapToCompound(compoundNBT, getPositionedTags());
        originalCompound.put(TAG_BLUEPRINTDATA, compoundNBT);
    }

    /**
     * Writes the given tag pos map to nbt
     *
     * @param compoundNBT compound to write to
     * @param tagPosMap   map to write
     */
    static void writeMapToCompound(final CompoundTag compoundNBT, Map<BlockPos, List<String>> tagPosMap)
    {
        ListTag tagPosList = new ListTag();
        for (Map.Entry<BlockPos, List<String>> entry : tagPosMap.entrySet())
        {
            CompoundTag posTagCompound = new CompoundTag();
            BlockPosUtil.writeToNBT(posTagCompound, TAG_TAG_POS, entry.getKey());

            final ListTag tagList = new ListTag();
            for (final String tag : entry.getValue())
            {
                CompoundTag tagCompound = new CompoundTag();
                tagCompound.putString(TAG_TAG_NAME, tag);
                tagList.add(tagCompound);
            }
            posTagCompound.put(TAG_TAG_NAME_LIST, tagList);

            tagPosList.add(posTagCompound);
        }

        compoundNBT.put(TAG_POS_TAG_MAP, tagPosList);
    }

    /**
     * Default read schematic data from nbt
     */
    default void readSchematicDataFromNBT(final CompoundTag originalCompound)
    {
        if (!originalCompound.contains(TAG_BLUEPRINTDATA))
        {
            return;
        }

        CompoundTag compoundNBT = originalCompound.getCompound(TAG_BLUEPRINTDATA);

        // Read schematic name
        setSchematicName(compoundNBT.getString(TAG_SCHEMATIC_NAME));

        // Read corners
        final BlockPos corner1 = BlockPosUtil.readFromNBT(compoundNBT, TAG_CORNER_ONE);
        final BlockPos corner2 = BlockPosUtil.readFromNBT(compoundNBT, TAG_CORNER_TWO);
        setSchematicCorners(corner1, corner2);

        // Read tagPosMap
        setPositionedTags(readTagPosMapFrom(compoundNBT));
    }

    /**
     * Reads the tagPosmap from nbt
     *
     * @param compoundNBT compound to read from
     * @return map of positions and tags
     */
    static Map<BlockPos, List<String>> readTagPosMapFrom(final CompoundTag compoundNBT)
    {
        final Map<BlockPos, List<String>> tagPosMap = new HashMap<>();
        if (!compoundNBT.contains(TAG_POS_TAG_MAP))
        {
            return tagPosMap;
        }

        final ListTag tagPosMapNBT = compoundNBT.getList(TAG_POS_TAG_MAP, Tag.TAG_COMPOUND);

        for (final Tag tagPosMapEntry : tagPosMapNBT)
        {
            if (!(tagPosMapEntry instanceof CompoundTag))
            {
                continue;
            }

            final CompoundTag entry = ((CompoundTag) tagPosMapEntry);
            final BlockPos tagPos = BlockPosUtil.readFromNBT(entry, TAG_TAG_POS);

            final Set<String> tagList = new HashSet<>();
            final ListTag tagListNbt = entry.getList(TAG_TAG_NAME_LIST, Tag.TAG_COMPOUND);

            for (final Tag tagEntryNBT : tagListNbt)
            {
                if (!(tagEntryNBT instanceof CompoundTag))
                {
                    continue;
                }

                final CompoundTag tagEntry = ((CompoundTag) tagEntryNBT);
                tagList.add(tagEntry.getString(TAG_TAG_NAME));
            }

            tagPosMap.put(tagPos, new ArrayList<>(tagList));
        }

        return tagPosMap;
    }

    /**
     * Removes a tag from the data
     *
     * @param tag to remove
     */
    default public void removeTag(final BlockPos pos, final String tag)
    {
        Map<BlockPos, List<String>> data = getPositionedTags();
        if (data.containsKey(pos))
        {
            data.get(pos).remove(tag);
            if (data.get(pos).isEmpty())
            {
                data.remove(pos);
            }
        }
        setPositionedTags(data);
    }

    /**
     * Removes a tag from the data
     *
     * @param tag to remove
     */
    default public void addTag(final BlockPos pos, final String tag)
    {
        Map<BlockPos, List<String>> data = getPositionedTags();
        if (data.containsKey(pos))
        {
            data.get(pos).add(tag);
        }
        else
        {
            List<String> list = new ArrayList<>();
            list.add(tag);
            data.put(pos, list);
        }
        setPositionedTags(data);
    }

    /**
     * Gets the tag pos map with real world coords
     *
     * @return the tag pos map with current world coords
     */
    default public Map<BlockPos, List<String>> getWorldTagPosMap()
    {
        final Map<BlockPos, List<String>> tagPosMap = new HashMap<>();

        for (final Map.Entry<BlockPos, List<String>> entry : getPositionedTags().entrySet())
        {
            tagPosMap.put(entry.getKey().offset(getTilePos()), entry.getValue());
        }

        return tagPosMap;
    }

    /**
     * Gets the tag name to positions map
     *
     * @return the tag pos map with current world coords
     */
    default public Map<String, Set<BlockPos>> getWorldTagNamePosMap()
    {
        final Map<String, Set<BlockPos>> tagNamePosMap = new HashMap<>();

        for (final Map.Entry<BlockPos, List<String>> entry : getPositionedTags().entrySet())
        {
            for (final String tagName : entry.getValue())
            {
                tagNamePosMap.computeIfAbsent(tagName, new Function<String, Set<BlockPos>>() {
                    @Override
                    public Set<BlockPos> apply(final String s)
                    {
                        return new HashSet<>();
                    }
                }).add(entry.getKey().offset(getTilePos()));
            }
        }

        return tagNamePosMap;
    }

    /**
     * Converts a relative pos to its real world pos
     *
     * @param relativePos
     * @return
     */
    default public BlockPos getRealWorldPos(final BlockPos relativePos)
    {
        return relativePos.offset(getTilePos());
    }

    /**
     * Gets the TE's world position
     *
     * @return position
     */
    public BlockPos getTilePos();

    /**
     * Gets the update packet, needed for initial placement through schematic paste
     *
     * @return client update packet
     */
    public ClientboundBlockEntityDataPacket getUpdatePacket();
}
