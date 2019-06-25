package com.ldtteam.structurize.util;

import com.ldtteam.structurize.util.constants.GeneralConstants;
import net.minecraft.util.ResourceLocation;

/**
 * Class for random util methods
 */
public final class Utils
{
    /**
     * Private constructor to hide implicit public one.
     */
    private Utils()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Creates a resource location from mod id and path.
     *
     * @param path path for the new RL
     * @return the new RL from mod id and given path
     */
    public static ResourceLocation createLocationFor(final String path)
    {
        return new ResourceLocation(GeneralConstants.MOD_ID, path);
    }
}
