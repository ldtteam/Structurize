package com.ldtteam.structurize.blueprints.v1;

import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;

import com.ldtteam.structurize.blocks.interfaces.IInvisibleBlueprintAnchorBlock;
import com.ldtteam.structurize.util.BlockInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDLEVEL_TAG;
import static com.ldtteam.structurize.api.util.constant.Constants.INVISIBLE_TAG;
import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;

/**
 * Utilities for locating tags in a blueprint
 */
public class BlueprintTagUtils
{
    /**
     * Gets the tag map from the given blueprint.
     *
     * @param blueprint the blueprint
     * @return          the tag map, relative to the anchor block
     */
    public static Map<BlockPos, List<String>> getBlueprintTags(final Blueprint blueprint)
    {
        final BlockPos anchorPos = blueprint.getPrimaryBlockOffset();
        final CompoundTag nbt = blueprint.getBlockInfoAsMap().get(anchorPos).getTileEntityData();

        if (nbt != null)
        {
            return IBlueprintDataProviderBE.readTagPosMapFrom(nbt.getCompound(TAG_BLUEPRINTDATA));
        }

        return new HashMap<>();
    }

    /**
     * A blueprint may hide itself from the build tool list in one of two ways:
     * 1. the anchor block implements IInvisibleBlueprintAnchorBlock and returns true when asked
     * 2. the anchor block implements IBlueprintDataProviderBE and is directly tagged "invisible"
     *
     * @param blueprint the blueprint to check
     * @return true if this blueprint should be hidden from normal players
     */
    public static boolean isInvisible(final Blueprint blueprint)
    {
        final BlockInfo anchor = blueprint.getBlockInfoAsMap().get(blueprint.getPrimaryBlockOffset());
        if (anchor.getState().getBlock() instanceof IInvisibleBlueprintAnchorBlock invis &&
                !invis.isVisible(anchor.getTileEntityData()))
        {
            return true;
        }

        final List<String> anchorTags = getBlueprintTags(blueprint).computeIfAbsent(BlockPos.ZERO, k -> new ArrayList<>());
        return anchorTags.contains(INVISIBLE_TAG);
    }

    /**
     * Get the first pos for the given tag
     *
     * @param blueprint rotated/mirrored blueprint
     * @param tagName   tag name
     * @return found position or null
     */
    @Nullable
    public static BlockPos getFirstPosForTag(final Blueprint blueprint, final String tagName)
    {
        final Map<BlockPos, List<String>> tagPosMap = getBlueprintTags(blueprint);

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
