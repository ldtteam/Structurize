package com.ldtteam.structurize.blockentities.interfaces;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Blueprint anchor that may not want to show up in the list.
 */
public interface IInvisibleBlueprintAnchorBlock
{
    /**
     * Check if the blueprint with this anchor is visible.
     * @param component potential block entity data.
     * @return true if so.
     */
    boolean isVisible(@Nullable final CompoundTag component);
}
