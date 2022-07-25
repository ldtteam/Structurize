package com.ldtteam.structurize.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
     * @param position the position it is played at.
     */
    public static void playSuccessSound(@NotNull final Player player, @NotNull final BlockPos position)
    {
        if (player instanceof ServerPlayer)
        {
            ((ServerPlayer) player).connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_BELL,
              SoundSource.NEUTRAL,
              position.getX(),
              position.getY(),
              position.getZ(),
              1.0f,
              1.0f));
        }
    }

    /**
     * Play an error sound.
     * @param player the player to play it for.
     * @param position the position it is played at.
     */
    public static void playErrorSound(@NotNull final Player player, @NotNull final BlockPos position)
    {
        if (player instanceof ServerPlayer)
        {
            ((ServerPlayer) player).connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_DIDGERIDOO,
              SoundSource.NEUTRAL,
              position.getX(),
              position.getY(),
              position.getZ(),
              1.0f,
              0.3f));
        }
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
}
