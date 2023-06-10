package com.ldtteam.structurize.blocks.interfaces;

import net.minecraft.nbt.CompoundTag;

/**
 * Blueprint anchor that levels.
 */
public interface ILeveledBlueprintAnchorBlock
{
    /**
     * Getter for the level.
     * @param beData potential block entity data.
     * @return the level.
     */
    int getLevel(final CompoundTag beData);
}
