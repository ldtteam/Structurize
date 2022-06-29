package com.ldtteam.structurize.blockentities.interfaces;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.List;

/**
 * Blueprint anchor that has a display name.
 */
public interface INamedBlueprintAnchorBlock
{
    /**
     * The name to display in the UI.
     * @return the name string.
     */
    Component getBlueprintDisplayName();

    /**
     * Get a description to display in the UI.
     * @return list of descriptions.
     */
    default List<MutableComponent> getDesc()
    {
        return Collections.emptyList();
    }
}
