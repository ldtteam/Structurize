package com.ldtteam.structurize.blockentities.interfaces;

import net.minecraft.network.chat.Component;

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
}
