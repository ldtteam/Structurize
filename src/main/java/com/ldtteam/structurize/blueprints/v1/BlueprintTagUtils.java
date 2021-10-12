package com.ldtteam.structurize.blueprints.v1;

import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDLEVEL_TAG;
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

    /**
     * Gets the number of layers at the bottom of the blueprint that are considered
     * 'ground' (or water, for ships).
     * @param blueprint The blueprint to query
     * @param defaultGroundLevels The number of levels to assume if there is no tag
     * @return The number of levels, e.g. 1 means that the bottom layer of the
     *         blueprint is ground and 3 means the bottom 3 layers are ground.
     */
    public static int getNumberOfGroundLevels(@NotNull final Blueprint blueprint,
      final int defaultGroundLevels)
    {
        final BlockPos groundLevelPos = getFirstPosForTag(blueprint, GROUNDLEVEL_TAG);
        if (groundLevelPos != null)
        {
            return blueprint.getPrimaryBlockOffset().getY() + groundLevelPos.getY() + 1;
        }

        return defaultGroundLevels;
    }

    /**
     * Gets the relative height difference between the blueprint's anchor position and
     * the 'ground' (or water, for ships).
     * @param blueprint The blueprint to query
     * @param defaultGroundOffset The height difference to assume if there is no tag
     * @return The height difference, e.g. 1 means that the ground level is 1 block below
     *         the anchor position.  The value may be negative (if the anchor is underground)
     *         or indicate a position outside the blueprint.
     */
    public static int getGroundAnchorOffset(@NotNull final Blueprint blueprint,
      final int defaultGroundOffset)
    {
        final BlockPos groundLevelPos = getFirstPosForTag(blueprint, GROUNDLEVEL_TAG);
        if (groundLevelPos != null)
        {
            return -groundLevelPos.getY();
        }

        return defaultGroundOffset;
    }

    /**
     * For a given blueprint, converts a "number of ground levels" value to a ground-anchor
     * relative height offset.
     * @param blueprint The associated blueprint
     * @param groundLevels The number of levels at the bottom of the blueprint that are 'ground'
     * @return The number of levels below the anchor at which 'ground" starts.  This might be
     *         negative if the anchor is underground.
     */
    public static int getGroundAnchorOffsetFromGroundLevels(@NotNull final Blueprint blueprint,
      final int groundLevels)
    {
        return blueprint.getPrimaryBlockOffset().getY() - groundLevels + 1;
    }
}
