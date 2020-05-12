package com.ldtteam.structurize.blocks.interfaces;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.*;

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
     * Gets the map of blockpos and its applied tags.
     *
     * @return
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
    public Tuple<BlockPos, BlockPos> getCornerPositions();

    /**
     * Sets the schematics corner positions
     *
     * @param pos1 first pos
     * @param pos2 second pos
     */
    public void setCorners(BlockPos pos1, BlockPos pos2);

    /**
     * Default write to nbt
     */
    default CompoundNBT writeSchematicDataToNBT()
    {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putString(TAG_SCHEMATIC_NAME, getSchematicName());
        BlockPosUtil.writeToNBT(compoundNBT, TAG_CORNER_ONE, getCornerPositions().getA());
        BlockPosUtil.writeToNBT(compoundNBT, TAG_CORNER_TWO, getCornerPositions().getB());

        ListNBT tagPosList = new ListNBT();
        for (Map.Entry<BlockPos, List<String>> entry : getPositionedTags().entrySet())
        {
            CompoundNBT posTagCompound = new CompoundNBT();
            BlockPosUtil.writeToNBT(posTagCompound, TAG_TAG_POS, entry.getKey());

            final ListNBT tagList = new ListNBT();
            for (final String tag : entry.getValue())
            {
                CompoundNBT tagCompound = new CompoundNBT();
                tagCompound.putString(TAG_TAG_NAME, tag);
                tagList.add(tagCompound);
            }
            posTagCompound.put(TAG_TAG_NAME_LIST, tagList);

            tagPosList.add(posTagCompound);
        }

        compoundNBT.put(TAG_POS_TAG_MAP, tagPosList);
        return compoundNBT;
    }

    /**
     * Default read schematic data from nbt
     *
     * @param compoundNBT compound to read from
     */
    default void readSchematicDataFromNBT(final CompoundNBT compoundNBT)
    {
        // Read schematic name
        setSchematicName(compoundNBT.getString(TAG_SCHEMATIC_NAME));

        // Read corners
        final BlockPos corner1 = BlockPosUtil.readFromNBT(compoundNBT, TAG_CORNER_ONE);
        final BlockPos corner2 = BlockPosUtil.readFromNBT(compoundNBT, TAG_CORNER_TWO);
        setCorners(corner1, corner2);

        // Read tagPosMap
        final Map<BlockPos, List<String>> tagPosMap = new HashMap<>();
        final ListNBT tagPosMapNBT = compoundNBT.getList(TAG_POS_TAG_MAP, Constants.NBT.TAG_COMPOUND);

        for (final INBT tagPosMapEntry : tagPosMapNBT)
        {
            if (!(tagPosMapEntry instanceof CompoundNBT))
            {
                continue;
            }

            final CompoundNBT entry = ((CompoundNBT) tagPosMapEntry);
            final BlockPos tagPos = BlockPosUtil.readFromNBT(entry, TAG_TAG_POS);

            final Set<String> tagList = new HashSet<>();
            final ListNBT tagListNbt = entry.getList(TAG_TAG_NAME_LIST, Constants.NBT.TAG_COMPOUND);

            for (final INBT tagEntryNBT : tagListNbt)
            {
                if (!(tagEntryNBT instanceof CompoundNBT))
                {
                    continue;
                }

                final CompoundNBT tagEntry = ((CompoundNBT) tagEntryNBT);
                tagList.add(tagEntry.getString(TAG_TAG_NAME));
            }

            tagPosMap.put(tagPos, new ArrayList<>(tagList));
        }

        setPositionedTags(tagPosMap);
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
}
