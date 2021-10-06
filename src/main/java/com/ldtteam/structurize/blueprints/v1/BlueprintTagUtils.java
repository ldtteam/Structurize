package com.ldtteam.structurize.blueprints.v1;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;

/**
 * Utilities for locating tags in a blueprint
 */
public class BlueprintTagUtils
{
    /**
     * Get the first pos for the given tag
     *
     * @param blueprint rotated/mirrored blueprint
     * @param tagName   tag name
     * @return found position or null
     */
    public static BlockPos getFirstPosForTag(final Blueprint blueprint, final String tagName)
    {
        final BlockPos anchorPos = blueprint.getPrimaryBlockOffset();
        final CompoundTag nbt = blueprint.getBlockInfoAsMap().get(anchorPos).getTileEntityData();
        if (nbt != null)
        {
            final Map<BlockPos, List<String>> tagPosMap = IBlueprintDataProvider.readTagPosMapFrom(nbt.getCompound(TAG_BLUEPRINTDATA));
            for (final Map.Entry<BlockPos, List<String>> entry : tagPosMap.entrySet())
            {
                for (final String tag : entry.getValue())
                {
                    if (tag.equals(tagName))
                    {
                        return entry.getKey();
                    }
                }
            }
        }

        return null;
    }
}
