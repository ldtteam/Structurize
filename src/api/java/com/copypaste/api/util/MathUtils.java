package com.copypaste.api.util;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Useful math stuff to use statically.
 */
public final class MathUtils
{
    private static final int NANO_TIME_DIVIDER = 1000 * 1000 * 1000;

    /**
     * Private constructor to hide the public one.
     */
    private MathUtils()
    {

    }

    /**
     * Reduces nanosecond time to seconds.
     *
     * @param nanoSeconds as input.
     * @return nanoSeconds to seconds.
     */
    public static long nanoSecondsToSeconds(final long nanoSeconds)
    {
        return nanoSeconds / NANO_TIME_DIVIDER;
    }
}
