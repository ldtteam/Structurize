package com.ldtteam.structurize.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * General purpose utilities class.
 */
public final class Utils
{
    /**
     * Private constructor to hide the implicit public one.
     */
    private Utils()
    {
    }

    /**
     * Play a success sound.
     * @param player the player to play it for.
     */
    public static void playSuccessSound(@NotNull final Player player)
    {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    /**
     * Play an error sound.
     * @param player the player to play it for.
     */
    public static void playErrorSound(@NotNull final Player player)
    {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.get(), SoundSource.NEUTRAL, 1.0f, 0.3f);
    }

    /**
     * Checks if directory exists, else creates it.
     *
     * @param directory the directory to check.
     */
    public static void checkDirectory(final File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
    }

    /**
     * Check if two compound tags are equal.
     * @param originTag the origin tag.
     * @param compareTag the tag to compare.
     * @param strictMatching strict matching.
     * @return true if they match.
     */
    public static boolean compareNBT(final CompoundTag originTag, final CompoundTag compareTag, final boolean strictMatching)
    {
        if (strictMatching && originTag.size() != compareTag.size())
        {
            return false;
        }

        for (final String childTagKey : originTag.getAllKeys())
        {
            final Tag originChildTag = originTag.get(childTagKey);
            final Tag compareChildTag = compareTag.get(childTagKey);

            if (!Objects.equals(originChildTag, compareChildTag))
            {
                return false;
            }
        }
        return true;
    }
}
